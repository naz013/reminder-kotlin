package com.elementary.tasks.core.services

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import com.elementary.tasks.birthdays.preview.ShowBirthdayActivity
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.utils.TimeUtil.BIRTH_FORMAT
import com.elementary.tasks.core.work.BackupDataWorker
import com.elementary.tasks.core.work.SyncDataWorker
import com.elementary.tasks.missed_calls.MissedCallDialogActivity
import com.elementary.tasks.reminder.work.CheckEventsWorker
import com.evernote.android.job.Job
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.*

class EventJobService : Job(), KoinComponent {

    private val prefs: Prefs by inject()
    private val notifier: Notifier by inject()
    private val appDb: AppDb by inject()

    override fun onRunJob(params: Params): Result {
        Timber.d("onRunJob: %s, tag -> %s", TimeUtil.getGmtFromDateTime(System.currentTimeMillis()), params.tag)
        val bundle = params.extras
        when (params.tag) {
            EventJobScheduler.EVENT_BIRTHDAY -> birthdayAction(context)
            EventJobScheduler.EVENT_BIRTHDAY_PERMANENT -> birthdayPermanentAction()
            EventJobScheduler.EVENT_AUTO_SYNC -> autoSyncAction()
            EventJobScheduler.EVENT_AUTO_BACKUP -> autoBackupAction()
            EventJobScheduler.EVENT_CHECK -> eventsCheckAction()
            else -> {
                when {
                    bundle.getBoolean(EventJobScheduler.ARG_MISSED, false) -> missedCallAction(params)
                    bundle.getBoolean(EventJobScheduler.ARG_LOCATION, false) -> SuperUtil.startGpsTracking(context)
                    bundle.getBoolean(EventJobScheduler.ARG_REPEAT, false) -> repeatedReminderAction(context, params.tag)
                    else -> reminderAction(context, params.tag)
                }
            }
        }
        return Result.SUCCESS
    }

    private fun repeatedReminderAction(context: Context, tag: String?) {
        val id = tag ?: ""
        val item = appDb.reminderDao().getById(id)
        if (item != null) {
            Timber.d("repeatedReminderAction: ${item.uuId}")
            notifier.showRepeatedNotification(context, item)
            EventJobScheduler.scheduleReminderRepeat(context, item.uuId, prefs)
        }
    }

    private fun eventsCheckAction() {
        CheckEventsWorker.schedule()
        EventJobScheduler.scheduleEventCheck(prefs)
    }

    private fun autoBackupAction() {
        BackupDataWorker.schedule()
        EventJobScheduler.scheduleAutoBackup(prefs)
    }

    private fun autoSyncAction() {
        SyncDataWorker.schedule()
        EventJobScheduler.scheduleAutoSync(prefs)
    }

    private fun birthdayPermanentAction() {
        if (prefs.isBirthdayPermanentEnabled) {
            notifier.showBirthdayPermanent()
        }
    }

    private fun missedCallAction(params: Params) {
        if (!prefs.applyDoNotDisturb(prefs.missedCallPriority)) {
            openMissedScreen(params.tag)
            EventJobScheduler.scheduleMissedCall(prefs, params.tag)
        } else if (prefs.doNotDisturbAction == 0) {
            EventJobScheduler.scheduleMissedCall(prefs, params.tag)
        }
    }

    private fun birthdayAction(context: Context) {
        EventJobScheduler.cancelDailyBirthday()
        EventJobScheduler.scheduleDailyBirthday(prefs)
        launchDefault {
            val daysBefore = prefs.daysToBirthday
            val applyDnd = prefs.applyDoNotDisturb(prefs.birthdayPriority)
            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()
            val mYear = cal.get(Calendar.YEAR)
            val mDate = BIRTH_FORMAT.format(cal.time)
            for (item in AppDb.getAppDatabase(context).birthdaysDao().all()) {
                val year = item.showedYear
                val birthValue = getBirthdayValue(item.month, item.day, daysBefore)
                if (!applyDnd && birthValue == mDate && year != mYear) {
                    withUIContext {
                        showBirthday(context, item)
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
            ReminderUtils.showSimpleBirthday(context, prefs, item.uuId)
        }
    }

    private fun openMissedScreen(tag: String) {
        val resultIntent = Intent(context, MissedCallDialogActivity::class.java)
        resultIntent.putExtra(Constants.INTENT_ID, tag)
        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        context.startActivity(resultIntent)
    }

    private fun reminderAction(context: Context, id: String) {
        val intent = Intent(context, ReminderActionReceiver::class.java)
        intent.action = ReminderActionReceiver.ACTION_RUN
        intent.putExtra(Constants.INTENT_ID, id)
        context.sendBroadcast(intent)
    }
}
