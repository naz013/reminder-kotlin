package com.elementary.tasks.core.services.action.birthday

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.elementary.tasks.core.data.repository.BirthdayRepository
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.DateValidator
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

class BirthdayActionProcessor(
    private val dispatcherProvider: DispatcherProvider,
    private val birthdayHandlerFactory: BirthdayHandlerFactory,
    private val birthdayRepository: BirthdayRepository,
    private val prefs: Prefs,
    private val doNotDisturbManager: DoNotDisturbManager,
    private val dateTimeManager: DateTimeManager,
    private val jobScheduler: JobScheduler,
    private val analyticsEventSender: AnalyticsEventSender,
    private val contextProvider: ContextProvider,
    private val dateValidator: DateValidator = DateValidator()
) {

  private val scope = CoroutineScope(dispatcherProvider.default())

  fun sendSms(id: String) {
    Logger.d("sendSms: $id")
    scope.launch {
      val birthday = birthdayRepository.getById(id) ?: return@launch
      birthdayHandlerFactory.createCancel().handle(birthday)
      withContext(dispatcherProvider.main()) {
        TelephonyUtil.sendSms(birthday.number, contextProvider.context)
      }
    }
  }

  fun makeCall(id: String) {
    Logger.d("makeCall: $id")
    scope.launch {
      val birthday = birthdayRepository.getById(id) ?: return@launch
      birthdayHandlerFactory.createCancel().handle(birthday)
      withContext(dispatcherProvider.main()) {
        TelephonyUtil.makeCall(birthday.number, contextProvider.context)
      }
    }
  }

  fun cancel(id: String) {
    Logger.d("cancel: $id")
    scope.launch {
      val birthday = birthdayRepository.getById(id) ?: return@launch
      birthdayHandlerFactory.createCancel().handle(birthday)
    }
  }

  fun process() {
    Logger.d("process: ")
    jobScheduler.cancelDailyBirthday()
    jobScheduler.scheduleDailyBirthday()
    scope.launch {
      val daysBefore = prefs.daysToBirthday
      val applyDnd = doNotDisturbManager.applyDoNotDisturb(prefs.birthdayPriority)

      val date = LocalDate.now()
      val mYear = date.year
      val currentDate = dateTimeManager.getBirthdayDateSearch(date)

      val handler =
        birthdayHandlerFactory.createAction(!SuperUtil.isPhoneCallActive(contextProvider.context))

      val birthdays = birthdayRepository.getAll()
        .filter { dateValidator.isLegacyMonthValid(it.month) }

      for (birthday in birthdays) {
        val year = birthday.showedYear
        val birthValue = getBirthdayValue(birthday.month, birthday.day, daysBefore)
        if (!applyDnd && birthValue == currentDate && year != mYear) {
          analyticsEventSender.send(FeatureUsedEvent(Feature.BIRTHDAY))
          withContext(dispatcherProvider.main()) {
            handler.handle(birthday)
          }
        }
      }
    }
  }

  private fun getBirthdayValue(month: Int, day: Int, daysBefore: Int): String {
    val date = LocalDate.now()
      .withMonth(month + 1)
      .withDayOfMonth(day)
      .minusDays(daysBefore.toLong())
    return dateTimeManager.getBirthdayDateSearch(date)
  }
}
