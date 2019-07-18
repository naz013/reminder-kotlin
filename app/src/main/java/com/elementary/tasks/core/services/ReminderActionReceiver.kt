package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elementary.tasks.Actions
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.reminder.preview.ReminderDialogActivity
import timber.log.Timber

class ReminderActionReceiver : BaseBroadcast() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            Timber.d("onReceive: $action")
            if (action != null) {
                when {
                    action.matches(ACTION_HIDE.toRegex()) -> hidePermanent(context, intent.getStringExtra(Constants.INTENT_ID) ?: "")
                    action.matches(ACTION_SNOOZE.toRegex()) -> snoozeReminder(context, intent.getStringExtra(Constants.INTENT_ID) ?: "")
                    action.matches(ACTION_RUN.toRegex()) -> {
                        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
                        resolveAction(context, id)
                    }
                    else -> showReminder(context, intent.getStringExtra(Constants.INTENT_ID) ?: "")
                }
            }
        }
    }

    private fun snoozeReminder(context: Context, id: String) {
        launchDefault {
            val reminder = AppDb.getAppDatabase(context).reminderDao().getById(id)
            if (reminder != null) {
                EventControlFactory.getController(reminder).setDelay(prefs.snoozeTime)
                endService(context, reminder.uniqueId)
            }
        }
    }

    private fun showReminder(context: Context, id: String) {
        val reminder = AppDb.getAppDatabase(context).reminderDao().getById(id) ?: return

        sendCloseBroadcast(context, id)

        if (Module.isQ) {
            qAction(reminder, context)
        } else {
            val notificationIntent = ReminderDialogActivity.getLaunchIntent(context, id)
            notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
            context.startActivity(notificationIntent)
            endService(context, reminder.uniqueId)
        }
    }

    private fun hidePermanent(context: Context, id: String) {
        val reminder = AppDb.getAppDatabase(context).reminderDao().getById(id) ?: return
        EventControlFactory.getController(reminder).next()
        ContextCompat.startForegroundService(context,
                EventOperationalService.getIntent(context, reminder.uuId,
                        EventOperationalService.TYPE_REMINDER,
                        EventOperationalService.ACTION_STOP,
                        reminder.uniqueId))
        endService(context, reminder.uniqueId)
    }

    private fun endService(context: Context, id: Int) {
        Notifier.getManager(context)?.cancel(id)
    }

    private fun sendCloseBroadcast(context: Context, id: String) {
        val intent = Intent(ReminderDialogActivity.ACTION_STOP_BG_ACTIVITY)
        intent.putExtra(Constants.INTENT_ID, id)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun resolveAction(context: Context, id: String) {
        launchDefault {
            var windowType = prefs.reminderType
            val ignore = prefs.isIgnoreWindowType
            val reminder = AppDb.getAppDatabase(context).reminderDao().getById(id) ?: return@launchDefault
            if (!ignore) {
                windowType = reminder.windowType
            }
            Timber.d("start: ignore -> $ignore, event -> $reminder")
            if (prefs.applyDoNotDisturb(reminder.priority)) {
                if (prefs.doNotDisturbAction == 0) {
                    val delayTime = TimeUtil.millisToEndDnd(prefs.doNotDisturbFrom, prefs.doNotDisturbTo, System.currentTimeMillis() - TimeCount.MINUTE)
                    if (delayTime > 0) {
                        EventJobScheduler.scheduleReminderDelay(delayTime, id)
                    }
                }
            } else {
                withUIContext {
                    if (Module.isQ) {
                        qAction(reminder, context)
                    } else {
                        if (windowType == 0) {
                            sendCloseBroadcast(context, id)
                            context.startActivity(ReminderDialogActivity.getLaunchIntent(context, id))
                        } else {
                            ReminderUtils.showSimpleReminder(context, prefs, id)
                        }
                    }
                }
            }
        }
    }

    private fun qAction(reminder: Reminder, context: Context) {
        sendCloseBroadcast(context, reminder.uuId)
        ContextCompat.startForegroundService(context,
                EventOperationalService.getIntent(context, reminder.uuId,
                        EventOperationalService.TYPE_REMINDER,
                        EventOperationalService.ACTION_PLAY,
                        reminder.uniqueId))
    }

    companion object {
        const val ACTION_SHOW = Actions.Reminder.ACTION_SHOW_FULL
        const val ACTION_HIDE = Actions.Reminder.ACTION_HIDE_SIMPLE
        const val ACTION_RUN = Actions.Reminder.ACTION_RUN
        const val ACTION_SNOOZE = Actions.Reminder.ACTION_SNOOZE

        fun showIntent(context: Context, id: String): Intent {
            val notificationIntent = Intent(context, ReminderActionReceiver::class.java)
            notificationIntent.action = ACTION_SHOW
            notificationIntent.putExtra(Constants.INTENT_ID, id)
            return notificationIntent
        }
    }
}
