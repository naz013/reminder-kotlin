package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.core.utils.ActivateAllActiveRemindersUseCase
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.logic.habit.nudge.RescheduleHabitNudgesUseCase
import com.github.naz013.scheduler.JobSchedulerApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.get
import org.koin.core.component.inject

class BootReceiver : BaseBroadcast() {
  private val jobScheduler by inject<JobSchedulerApi>()

  override fun onReceive(
    context: Context,
    intent: Intent,
  ) {
    Logger.i(TAG, "Received ${intent.action}")
    if (intent.action in HABIT_RESCHEDULE_ACTIONS) {
      rescheduleHabitNudges()
    }
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
      get<ActivateAllActiveRemindersUseCase>().run()
      if (prefs.isBirthdayReminderEnabled) {
        jobScheduler.scheduleDailyBirthday()
      }
      if (prefs.isSbNotificationEnabled) {
        PermanentReminderReceiver.show(context)
      }
      if (prefs.isContactAutoCheckEnabled) {
        jobScheduler.scheduleBirthdaysCheck()
      }
      jobScheduler.scheduleAutoBackup()
      if (prefs.isBirthdayPermanentEnabled) {
        jobScheduler.scheduleBirthdayPermanent()
        notifier.showBirthdayPermanent()
      }
    }
  }

  private fun rescheduleHabitNudges() {
    val pendingResult = goAsync()
    CoroutineScope(get<DispatcherProvider>().io()).launch {
      try {
        get<RescheduleHabitNudgesUseCase>()()
      } finally {
        pendingResult.finish()
      }
    }
  }

  companion object {
    private val HABIT_RESCHEDULE_ACTIONS =
      setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
      )
    private const val TAG = "BootReceiver"
  }
}
