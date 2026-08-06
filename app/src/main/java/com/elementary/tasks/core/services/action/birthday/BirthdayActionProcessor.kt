package com.elementary.tasks.core.services.action.birthday

import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.telephony.PhoneCaller
import com.elementary.tasks.telephony.SmsSender
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.Permissions
import com.github.naz013.datecalc.BirthdayDateCalculator
import com.github.naz013.datecalc.BirthdayDateCalculatorImpl
import com.github.naz013.datecalc.DateValidator
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
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
  private val jobScheduler: JobScheduler,
  private val analyticsEventSender: AnalyticsEventSender,
  private val contextProvider: ContextProvider,
  private val dateValidator: DateValidator = DateValidator(),
  private val birthdayDateCalculator: BirthdayDateCalculator = BirthdayDateCalculatorImpl(),
  private val smsSender: SmsSender,
  private val phoneCaller: PhoneCaller,
) {
  private val scope = CoroutineScope(dispatcherProvider.default())

  fun sendSms(id: String) {
    Logger.d(TAG, "sendSms: $id")
    scope.launch {
      val birthday = birthdayRepository.getById(id) ?: return@launch
      birthdayHandlerFactory.createCancel().handle(birthday)
      withContext(dispatcherProvider.main()) {
        smsSender.send(birthday.number, null)
      }
    }
  }

  fun makeCall(id: String) {
    Logger.d(TAG, "makeCall: $id")
    scope.launch {
      val birthday = birthdayRepository.getById(id) ?: return@launch
      birthdayHandlerFactory.createCancel().handle(birthday)
      withContext(dispatcherProvider.main()) {
        if (Permissions.checkPermission(contextProvider.context, Permissions.CALL_PHONE)) {
          phoneCaller.call(birthday.number)
        }
      }
    }
  }

  fun cancel(id: String) {
    Logger.d(TAG, "cancel: $id")
    scope.launch {
      val birthday = birthdayRepository.getById(id) ?: return@launch
      birthdayHandlerFactory.createCancel().handle(birthday)
    }
  }

  suspend fun process() {
    Logger.d(TAG, "process: ")
    jobScheduler.cancelDailyBirthday()
    jobScheduler.scheduleDailyBirthday()
    scope.launch {
      val daysBefore = prefs.daysToBirthday
      val applyDnd = doNotDisturbManager.applyDoNotDisturb(prefs.birthdayPriority)

      val date = LocalDate.now()
      val mYear = date.year

      val handler =
        birthdayHandlerFactory.createAction(!SuperUtil.isPhoneCallActive(contextProvider.context))

      val birthdays =
        birthdayRepository
          .getAll()
          .filter { dateValidator.isLegacyMonthValid(it.month) }

      for (birthday in birthdays) {
        val year = birthday.showedYear
        val isBirthdayToday =
          birthdayDateCalculator.isBirthdayOn(
            birthMonth1Based = birthday.month + 1,
            birthDay = birthday.day,
            targetDate = date,
            daysBefore = daysBefore,
          )
        if (!applyDnd && isBirthdayToday && year != mYear) {
          analyticsEventSender.send(FeatureUsedEvent(Feature.BIRTHDAY))
          withContext(dispatcherProvider.main()) {
            handler.handle(birthday)
          }
        }
      }
    }
  }

  companion object {
    private const val TAG = "BirthdayActionProcessor"
  }
}
