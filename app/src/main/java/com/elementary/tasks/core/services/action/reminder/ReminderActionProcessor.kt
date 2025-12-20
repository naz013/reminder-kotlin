package com.elementary.tasks.core.services.action.reminder

import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.SuperUtil
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.ContextProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime

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
    Logger.i(TAG, "Snoozing reminder: $id")
    scope.launch {
      val reminder = reminderRepository.getById(id) ?: return@launch
      withContext(dispatcherProvider.main()) {
        reminderHandlerFactory.createSnooze().handle(reminder)
      }
    }
  }

  fun complete(id: String) {
    Logger.i(TAG, "Completing reminder: $id")
    scope.launch {
      val reminder = reminderRepository.getById(id) ?: return@launch
      jobScheduler.cancelReminder(reminder.uniqueId)
      withContext(dispatcherProvider.main()) {
        reminderHandlerFactory.createComplete().handle(reminder)
      }
    }
  }

  fun process(id: String) {
    Logger.i(TAG, "Going to process reminder: $id")
    scope.launch {
      val reminder = reminderRepository.getById(id) ?: return@launch
      if (doNotDisturbManager.applyDoNotDisturb(reminder.priority)) {
        if (prefs.doNotDisturbAction == 0) {
          val delayTime = dateTimeManager.millisToEndDnd(
            prefs.doNotDisturbFrom,
            prefs.doNotDisturbTo,
            LocalDateTime.now().minusMinutes(1)
          )
          if (delayTime > 0) {
            Logger.i(TAG, "Delaying reminder id=${reminder.uuId} for $delayTime ms due to DND")
            jobScheduler.scheduleReminderDelay(delayTime, id, reminder.uniqueId)
          }
        } else {
          Logger.w(TAG, "Skipping reminder id=${reminder.uuId} due to DND settings")
        }
      } else {
        val canShowWindow = !SuperUtil.isPhoneCallActive(contextProvider.context)
        analyticsEventSender.send(FeatureUsedEvent(Feature.REMINDER))
        val handler = reminderHandlerFactory.createAction(canShowWindow)
        Logger.d(TAG, "Processing reminder id=${reminder.uuId} with handler ${handler.javaClass.simpleName}")
        withContext(dispatcherProvider.main()) {
          handler.handle(reminder)
        }
      }
    }
  }

  companion object {
    private const val TAG = "ReminderActionProcessor"
  }
}
