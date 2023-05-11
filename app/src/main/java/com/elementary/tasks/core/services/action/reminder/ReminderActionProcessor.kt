package com.elementary.tasks.core.services.action.reminder

import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.data.repository.ReminderRepository
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.params.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import timber.log.Timber

class ReminderActionProcessor(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderHandlerFactory: ReminderHandlerFactory,
  private val reminderRepository: ReminderRepository,
  private val prefs: Prefs,
  private val doNotDisturbManager: DoNotDisturbManager,
  private val dateTimeManager: DateTimeManager,
  private val jobScheduler: JobScheduler,
  private val contextProvider: ContextProvider,
  private val analyticsEventSender: AnalyticsEventSender
) {

  private val scope = CoroutineScope(dispatcherProvider.default())

  fun snooze(id: String) {
    Timber.d("snooze: $id")
    scope.launch {
      val reminder = reminderRepository.getById(id) ?: return@launch
      withContext(dispatcherProvider.main()) {
        reminderHandlerFactory.createSnooze().handle(reminder)
      }
    }
  }

  fun cancel(id: String) {
    Timber.d("cancel: $id")
    scope.launch {
      jobScheduler.cancelReminder(id)
      val reminder = reminderRepository.getById(id) ?: return@launch
      withContext(dispatcherProvider.main()) {
        reminderHandlerFactory.createCancel().handle(reminder)
      }
    }
  }

  fun process(id: String) {
    Timber.d("process: $id")
    scope.launch {
      val reminder = reminderRepository.getById(id) ?: return@launch

      var windowType = prefs.reminderType
      val ignore = prefs.isIgnoreWindowType

      if (!ignore) {
        windowType = reminder.windowType
      }
      Timber.d("process: ignore -> $ignore, event -> $reminder")
      if (doNotDisturbManager.applyDoNotDisturb(reminder.priority)) {
        if (prefs.doNotDisturbAction == 0) {
          val delayTime = dateTimeManager.millisToEndDnd(
            prefs.doNotDisturbFrom,
            prefs.doNotDisturbTo,
            LocalDateTime.now().minusMinutes(1)
          )
          if (delayTime > 0) {
            jobScheduler.scheduleReminderDelay(delayTime, id)
          }
        }
      } else {
        val canShowWindow = windowType == 0 && !SuperUtil.isPhoneCallActive(contextProvider.context)
        analyticsEventSender.send(FeatureUsedEvent(Feature.REMINDER))
        val handler = reminderHandlerFactory.createAction(canShowWindow)
        Timber.d("process: handler=$handler")
        withContext(dispatcherProvider.main()) {
          handler.handle(reminder)
        }
      }
    }
  }
}
