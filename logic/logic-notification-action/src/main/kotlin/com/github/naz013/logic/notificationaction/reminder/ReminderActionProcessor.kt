package com.github.naz013.logic.notificationaction.reminder

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderPriority
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
import com.github.naz013.logic.reminder.ReminderPreferences
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
  private val reminderPreferences: ReminderPreferences,
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

  /**
   * @param repeatCount how many times this specific firing has already re-alerted (0 for the
   * original fire). Once it reaches [ReminderPreferences.escalateAfterRepeats], delivery is
   * escalated - forced past Do Not Disturb, at max priority, waking the screen - since the whole
   * point of a repeat that's gone unacknowledged this many times is to stop being ignorable.
   */
  fun process(
    id: String,
    repeatCount: Int = 0,
  ) {
    Logger.i(TAG, "Going to process reminder: $id, repeatCount=$repeatCount")
    scope.launch {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      val resolved = resolveReminderV2NotificationSettingsUseCase(reminder)
      val isEscalated = repeatCount > 0 && repeatCount >= reminderPreferences.escalateAfterRepeats
      val effective = if (isEscalated) escalate(resolved) else resolved
      val priority = effective.priority.ordinal
      if (!effective.bypassDoNotDisturb && doNotDisturbManager.applyDoNotDisturb(priority)) {
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
        val handler = alertHandlerFactory.create(canShowWindow, effective)
        Logger.d(TAG, "Processing reminder id=${reminder.uuId} with handler $handler")
        withContext(dispatcherProvider.main()) {
          handler.handle(reminder)
          if (foregroundStateTracker.isForeground.value && inAppAlertPreferences.isInAppAlertBannerEnabled) {
            inAppAlertBus.show(buildInAppAlert(reminder))
          }
        }
        reminderV2Repository.save(reminder.copy(lastShownAt = dateTimeManager.localToUtc(LocalDateTime.now())))
        if (resolved.repeatNotification && repeatCount < reminderPreferences.maxRepeatCount) {
          Logger.d(TAG, "Scheduling repeat #${repeatCount + 1} for reminder id=${reminder.uuId}")
          jobScheduler.scheduleReminderRepeat(reminder, repeatCount + 1)
        }
      }
    }
  }

  private fun escalate(settings: NotificationSettings): NotificationSettings =
    settings.copy(
      bypassDoNotDisturb = true,
      wakeScreen = true,
      priority = ReminderPriority.HIGHEST,
    )

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
