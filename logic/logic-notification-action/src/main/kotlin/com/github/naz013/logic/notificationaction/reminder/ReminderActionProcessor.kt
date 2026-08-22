package com.github.naz013.logic.notificationaction.reminder

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.logic.notificationaction.DoNotDisturbManager
import com.github.naz013.logic.notificationaction.DoNotDisturbPreferences
import com.github.naz013.logic.notificationaction.ForegroundStateTracker
import com.github.naz013.logic.notificationaction.InAppAlert
import com.github.naz013.logic.notificationaction.InAppAlertAction
import com.github.naz013.logic.notificationaction.InAppAlertBus
import com.github.naz013.logic.notificationaction.InAppAlertDomain
import com.github.naz013.logic.notificationaction.InAppAlertPreferences
import com.github.naz013.logic.notificationaction.PhoneCallStateProvider
import com.github.naz013.logic.reminder.query.ResolveReminderV2NotificationSettingsUseCase
import com.github.naz013.logic.workflow.WorkflowTriggerRunner
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.icon.DrawableCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime

class ReminderActionProcessor(
  private val dispatcherProvider: DispatcherProvider,
  private val alertHandlerFactory: ReminderAlertHandlerFactory,
  private val completeSnoozeFactory: ReminderCompleteSnoozeFactory,
  private val reminderV2Repository: ReminderV2Repository,
  private val doNotDisturbPreferences: DoNotDisturbPreferences,
  private val doNotDisturbManager: DoNotDisturbManager,
  private val dateTimeManager: DateTimeManager,
  private val jobScheduler: JobSchedulerApi,
  private val phoneCallStateProvider: PhoneCallStateProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val workflowTriggerRunner: WorkflowTriggerRunner,
  private val resolveReminderV2NotificationSettingsUseCase: ResolveReminderV2NotificationSettingsUseCase,
  private val inAppAlertBus: InAppAlertBus,
  private val foregroundStateTracker: ForegroundStateTracker,
  private val inAppAlertPreferences: InAppAlertPreferences,
  private val textProvider: TextProvider,
) {
  private val scope = CoroutineScope(dispatcherProvider.default())

  fun snooze(id: String) {
    Logger.i(TAG, "Snoozing reminder: $id")
    scope.launch {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      withContext(dispatcherProvider.main()) {
        completeSnoozeFactory.createSnooze().handle(reminder)
      }
    }
  }

  fun complete(id: String) {
    Logger.i(TAG, "Completing reminder: $id")
    scope.launch {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      jobScheduler.cancelReminder(reminder.uniqueId)
      withContext(dispatcherProvider.main()) {
        completeSnoozeFactory.createComplete().handle(reminder)
      }
      workflowTriggerRunner.onReminderCompleted(id)
    }
  }

  fun process(id: String) {
    Logger.i(TAG, "Going to process reminder: $id")
    scope.launch {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      val notificationSettings = resolveReminderV2NotificationSettingsUseCase(reminder)
      val priority = notificationSettings.priority.ordinal
      if (!notificationSettings.bypassDoNotDisturb && doNotDisturbManager.applyDoNotDisturb(priority)) {
        if (doNotDisturbPreferences.doNotDisturbAction == 0) {
          val delayTime =
            dateTimeManager.millisToEndDnd(
              doNotDisturbPreferences.doNotDisturbFrom,
              doNotDisturbPreferences.doNotDisturbTo,
              LocalDateTime.now().minusMinutes(1),
            )
          if (delayTime > 0) {
            Logger.i(TAG, "Delaying reminder id=${reminder.uuId} for $delayTime ms due to DND")
            jobScheduler.scheduleReminderDelay(delayTime, id, reminder.uniqueId)
          }
        } else {
          Logger.w(TAG, "Skipping reminder id=${reminder.uuId} due to DND settings")
        }
      } else {
        val canShowWindow = !phoneCallStateProvider.isPhoneCallActive()
        analyticsEventSender.send(FeatureUsedEvent(Feature.REMINDER))
        val handler = alertHandlerFactory.create(canShowWindow, notificationSettings)
        Logger.d(TAG, "Processing reminder id=${reminder.uuId} with handler $handler")
        withContext(dispatcherProvider.main()) {
          handler.handle(reminder)
          if (foregroundStateTracker.isForeground.value && inAppAlertPreferences.isInAppAlertBannerEnabled) {
            inAppAlertBus.show(buildInAppAlert(reminder))
          }
        }
        reminderV2Repository.save(reminder.copy(lastShownAt = dateTimeManager.localToUtc(LocalDateTime.now())))
      }
    }
  }

  /** Mirrors [com.elementary.tasks.core.services.action.reminder.process.ReminderNotificationHandler]'s
   *  content/actions so the banner reads the same as the system notification it accompanies. */
  private fun buildInAppAlert(reminder: ReminderV2): InAppAlert {
    val actions =
      mutableListOf(
        InAppAlertAction(
          iconRes = DrawableCatalog.Fluent.Checkmark,
          label = textProvider.getText(R.string.ok),
          onClick = { complete(reminder.uuId) },
        ),
      )
    if (reminder.places.isEmpty()) {
      actions +=
        InAppAlertAction(
          iconRes = DrawableCatalog.Fluent.Snooze,
          label = textProvider.getText(R.string.acc_button_snooze),
          onClick = { snooze(reminder.uuId) },
        )
    }
    return InAppAlert(
      alertId = reminder.uuId,
      domain = InAppAlertDomain.REMINDER,
      title = reminder.summary,
      text = null,
      iconRes = DrawableCatalog.Fluent.Alert,
      actions = actions,
    )
  }

  companion object {
    private const val TAG = "ReminderActionProcessor"
  }
}
