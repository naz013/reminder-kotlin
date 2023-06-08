package com.elementary.tasks.core.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.services.action.birthday.BirthdayActionProcessor
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.work.BackupDataWorker
import com.elementary.tasks.core.work.SyncDataWorker
import com.elementary.tasks.reminder.work.CheckEventsWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class EventJobService(
  private val context: Context,
  private val params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

  private val prefs by inject<Prefs>()
  private val notifier by inject<Notifier>()
  private val jobScheduler by inject<JobScheduler>()
  private val dateTimeManager by inject<DateTimeManager>()
  private val birthdayActionProcessor by inject<BirthdayActionProcessor>()

  override suspend fun doWork(): Result {
    Timber.d(
      "onRunJob: %s, tag -> %s",
      dateTimeManager.logDateTime(),
      params.tags.toList()
    )
    when (params.tags.first()) {
      JobScheduler.EVENT_BIRTHDAY -> birthdayAction()
      JobScheduler.EVENT_BIRTHDAY_PERMANENT -> birthdayPermanentAction()
      JobScheduler.EVENT_AUTO_SYNC -> autoSyncAction()
      JobScheduler.EVENT_AUTO_BACKUP -> autoBackupAction()
      JobScheduler.EVENT_CHECK -> eventsCheckAction()
      else -> {
      }
    }
    return Result.success()
  }

  private fun eventsCheckAction() {
    CheckEventsWorker.schedule(context)
    jobScheduler.scheduleEventCheck()
  }

  private fun autoBackupAction() {
    BackupDataWorker.schedule(context)
    jobScheduler.scheduleAutoBackup()
  }

  private fun autoSyncAction() {
    SyncDataWorker.schedule(context)
    jobScheduler.scheduleAutoSync()
  }

  private fun birthdayPermanentAction() {
    if (prefs.isBirthdayPermanentEnabled) {
      notifier.showBirthdayPermanent()
    }
  }

  private fun birthdayAction() {
    birthdayActionProcessor.process()
  }
}
