package com.elementary.tasks.core.services

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.birthdays.preview.ShowBirthdayActivity
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.TimeUtil.BIRTH_FORMAT
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.work.BackupDataWorker
import com.elementary.tasks.core.work.SyncDataWorker
import com.elementary.tasks.missed_calls.MissedCallDialogActivity
import com.elementary.tasks.reminder.work.CheckEventsWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.Calendar

class EventJobService(
  private val context: Context,
  private val params: WorkerParameters
) : Worker(context, params), KoinComponent {

  private val prefs by inject<Prefs>()
  private val appDb by inject<AppDb>()
  private val notifier by inject<Notifier>()
  private val jobScheduler by inject<JobScheduler>()

  override fun doWork(): Result {
    Timber.d(
      "onRunJob: %s, tag -> %s",
      TimeUtil.getGmtFromDateTime(System.currentTimeMillis()),
      params.tags.toList()
    )
    val bundle = params.inputData
    val tag = params.tags.first()
    when (tag) {
      JobScheduler.EVENT_BIRTHDAY -> birthdayAction(context)
      JobScheduler.EVENT_BIRTHDAY_PERMANENT -> birthdayPermanentAction()
      JobScheduler.EVENT_AUTO_SYNC -> autoSyncAction()
      JobScheduler.EVENT_AUTO_BACKUP -> autoBackupAction()
      JobScheduler.EVENT_CHECK -> eventsCheckAction()
      else -> {
        when {
          bundle.getBoolean(JobScheduler.ARG_MISSED, false) -> missedCallAction(tag)
          bundle.getBoolean(JobScheduler.ARG_LOCATION, false) -> SuperUtil.startGpsTracking(context)
          bundle.getBoolean(JobScheduler.ARG_REPEAT, false) -> repeatedReminderAction(
            context,
            tag
          )

          else -> reminderAction(context, tag)
        }
      }
    }
    return Result.success()
  }

  private fun repeatedReminderAction(context: Context, tag: String?) {
    val id = tag ?: ""
    val item = appDb.reminderDao().getById(id)
    if (item != null) {
      Timber.d("repeatedReminderAction: ${item.uuId}")
      reminderAction(context, item.uuId)
      jobScheduler.scheduleReminderRepeat(appDb, item.uuId)
    }
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

  private fun missedCallAction(tag: String) {
    if (!prefs.applyDoNotDisturb(prefs.missedCallPriority)) {
      jobScheduler.scheduleMissedCall(tag)
      if (Module.is10 || SuperUtil.isPhoneCallActive(context)) {
        ContextCompat.startForegroundService(
          context,
          EventOperationalService.getIntent(
            context,
            tag,
            EventOperationalService.TYPE_MISSED,
            EventOperationalService.ACTION_PLAY,
            0
          )
        )
      } else {
        openMissedScreen(tag)
      }
    } else if (prefs.doNotDisturbAction == 0) {
      jobScheduler.scheduleMissedCall(tag)
    }
  }

  private fun birthdayAction(context: Context) {
    jobScheduler.cancelDailyBirthday()
    jobScheduler.scheduleDailyBirthday()
    launchDefault {
      val daysBefore = prefs.daysToBirthday
      val applyDnd = prefs.applyDoNotDisturb(prefs.birthdayPriority)
      val cal = Calendar.getInstance()
      cal.timeInMillis = System.currentTimeMillis()
      val mYear = cal.get(Calendar.YEAR)
      val mDate = BIRTH_FORMAT.format(cal.time)
      for (item in appDb.birthdaysDao().all()) {
        val year = item.showedYear
        val birthValue = getBirthdayValue(item.month, item.day, daysBefore)
        if (!applyDnd && birthValue == mDate && year != mYear) {
          withUIContext {
            if (Module.is10) {
              ContextCompat.startForegroundService(
                context,
                EventOperationalService.getIntent(
                  context, item.uuId,
                  EventOperationalService.TYPE_BIRTHDAY,
                  EventOperationalService.ACTION_PLAY,
                  item.uniqueId
                )
              )
            } else {
              showBirthday(context, item)
            }
          }
        }
      }
    }
  }

  private fun getBirthdayValue(month: Int, day: Int, daysBefore: Int): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, day)
    calendar.timeInMillis = calendar.timeInMillis - AlarmManager.INTERVAL_DAY * daysBefore
    return BIRTH_FORMAT.format(calendar.time)
  }

  private fun showBirthday(context: Context, item: Birthday) {
    if (prefs.reminderType == 0) {
      context.startActivity(ShowBirthdayActivity.getLaunchIntent(context, item.uuId))
    } else {
      notifier.showSimpleBirthday(item.uuId)
    }
  }

  private fun openMissedScreen(tag: String) {
    val resultIntent = MissedCallDialogActivity.getLaunchIntent(context, tag)
    context.startActivity(resultIntent)
  }

  private fun reminderAction(context: Context, id: String) {
    val intent = Intent(context, ReminderActionReceiver::class.java)
    intent.action = ReminderActionReceiver.ACTION_RUN
    intent.putExtra(Constants.INTENT_ID, id)
    context.sendBroadcast(intent)
  }
}
