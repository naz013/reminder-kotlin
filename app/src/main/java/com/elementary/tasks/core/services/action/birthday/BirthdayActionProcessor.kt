package com.elementary.tasks.core.services.action.birthday

import com.elementary.tasks.ForegroundStateTracker
import com.elementary.tasks.R
import com.elementary.tasks.core.services.action.inapp.InAppAlert
import com.elementary.tasks.core.services.action.inapp.InAppAlertAction
import com.elementary.tasks.core.services.action.inapp.InAppAlertBus
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.ui.common.compose.foundation.telephony.PhoneCaller
import com.github.naz013.ui.common.compose.foundation.telephony.SmsSender
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.Permissions
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.BirthdayDateCalculator
import com.github.naz013.datecalc.DateValidator
import com.github.naz013.domain.Birthday
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.common.icon.DrawableCatalog
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
  private val jobScheduler: JobSchedulerApi,
  private val analyticsEventSender: AnalyticsEventSender,
  private val contextProvider: ContextProvider,
  private val dateValidator: DateValidator,
  private val birthdayDateCalculator: BirthdayDateCalculator,
  private val smsSender: SmsSender,
  private val phoneCaller: PhoneCaller,
  private val inAppAlertBus: InAppAlertBus,
  private val foregroundStateTracker: ForegroundStateTracker,
  private val textProvider: TextProvider,
  private val modelDateTimeFormatter: ModelDateTimeFormatter,
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
            if (foregroundStateTracker.isForeground.value) {
              inAppAlertBus.show(buildInAppAlert(birthday))
            }
          }
        }
      }
    }
  }

  /** Mirrors [com.elementary.tasks.core.services.action.birthday.process.BirthdayNotificationHandler]'s
   *  content/actions so the banner reads the same as the system notification it accompanies. Since
   *  this runs inside a loop over every birthday due today, [InAppAlertBus]'s latest-wins semantics
   *  mean only the last one processed ends up shown - no extra dedup needed here. */
  private fun buildInAppAlert(birthday: Birthday): InAppAlert {
    val actions =
      mutableListOf(
        InAppAlertAction(
          iconRes = DrawableCatalog.Fluent.Checkmark,
          label = textProvider.getText(R.string.ok),
          onClick = { cancel(birthday.uuId) },
        ),
      )
    if (birthday.number.isNotEmpty()) {
      actions +=
        InAppAlertAction(
          iconRes = DrawableCatalog.Fluent.Phone,
          label = textProvider.getText(R.string.make_call),
          onClick = { makeCall(birthday.uuId) },
        )
      actions +=
        InAppAlertAction(
          iconRes = DrawableCatalog.Fluent.Chat,
          label = textProvider.getText(R.string.send_sms),
          onClick = { sendSms(birthday.uuId) },
        )
    }
    return InAppAlert(
      alertId = birthday.uuId,
      title = birthday.name,
      text = if (!birthday.ignoreYear) modelDateTimeFormatter.getAgeFormatted(birthday.date) else null,
      iconRes = DrawableCatalog.Fluent.FoodCake,
      actions = actions,
    )
  }

  companion object {
    private const val TAG = "BirthdayActionProcessor"
  }
}
