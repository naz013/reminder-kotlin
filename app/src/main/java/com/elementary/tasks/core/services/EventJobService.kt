package com.elementary.tasks.core.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.services.action.birthday.BirthdayActionProcessor
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.work.BackupDataWorker
import com.elementary.tasks.core.work.SyncDataWorker
import com.elementary.tasks.reminder.work.CheckEventsWorker
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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
    Logger.d("onRunJob: ${dateTimeManager.logDateTime()}, tag -> ${params.tags.toList()}")
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

  private suspend fun birthdayAction() {
    birthdayActionProcessor.process()
  }
}
