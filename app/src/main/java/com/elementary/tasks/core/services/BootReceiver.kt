package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.core.utils.ActivateAllActiveRemindersUseCase
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.logic.habit.nudge.RescheduleHabitNudgesUseCase
import com.github.naz013.logic.reminder.usecase.HandleZoneChangeUseCase
import com.github.naz013.scheduler.JobSchedulerApi
import kotlinx.coroutines.CancellationException
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
    runAsyncWork(
      rescheduleHabits = intent.action in HABIT_RESCHEDULE_ACTIONS,
      reactivateReminders = intent.action in REMINDER_REACTIVATE_ACTIONS,
      isBoot = intent.action == Intent.ACTION_BOOT_COMPLETED,
    )
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
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

  // goAsync() hands out the PendingResult only once per onReceive; a second call returns null.
  private fun runAsyncWork(
    rescheduleHabits: Boolean,
    reactivateReminders: Boolean,
    isBoot: Boolean,
  ) {
    if (!rescheduleHabits && !reactivateReminders) return
    val pendingResult = goAsync()
    CoroutineScope(get<DispatcherProvider>().io()).launch {
      try {
        if (rescheduleHabits) {
          runSafely("Failed to reschedule habit nudges") {
            get<RescheduleHabitNudgesUseCase>()()
          }
        }
        if (reactivateReminders) {
          runSafely("Failed to handle zone change") {
            handleZoneAndReactivateReminders(isBoot)
          }
        }
      } finally {
        pendingResult?.finish()
      }
    }
  }

  private suspend fun handleZoneAndReactivateReminders(isBoot: Boolean) {
    val zoneChanged = get<HandleZoneChangeUseCase>()()
    if (shouldActivateReminders(isBoot, zoneChanged)) {
      get<ActivateAllActiveRemindersUseCase>()()
    }
  }

  private suspend fun runSafely(
    errorMessage: String,
    block: suspend () -> Unit,
  ) {
    try {
      block()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Logger.e(TAG, errorMessage, e)
    }
  }

  companion object {
    private val REMINDER_REACTIVATE_ACTIONS =
      setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
      )
    private val HABIT_RESCHEDULE_ACTIONS =
      setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
      )
    private const val TAG = "BootReceiver"
  }
}

internal fun shouldActivateReminders(
  isBoot: Boolean,
  zoneChanged: Boolean,
): Boolean = isBoot || zoneChanged
