package com.elementary.tasks.core.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.elementary.tasks.Actions
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.reminder.ReminderUpdateEvent
import com.elementary.tasks.reminder.preview.ReminderDialogActivity

import org.greenrobot.eventbus.EventBus

import androidx.core.app.NotificationManagerCompat
import timber.log.Timber

/**
 * Copyright 2017 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class ReminderActionService : BroadcastReceiver() {

    private fun showReminder(context: Context, id: Int) {
        val reminder = AppDb.getAppDatabase(context).reminderDao().getById(id) ?: return
        val notificationIntent = ReminderDialogActivity.getLaunchIntent(context, id)
        notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
        context.startActivity(notificationIntent)
        endService(context, reminder.uniqueId)
    }

    private fun hidePermanent(context: Context, id: Int) {
        val reminder = AppDb.getAppDatabase(context).reminderDao().getById(id) ?: return
        EventControlFactory.getController(reminder).next()
        EventBus.getDefault().post(ReminderUpdateEvent())
        endService(context, reminder.uniqueId)
    }

    private fun endService(context: Context, id: Int) {
        val mNotifyMgr = NotificationManagerCompat.from(context)
        mNotifyMgr.cancel(id)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            LogUtil.d(TAG, "onStartCommand: " + action!!)
            if (action != null) {
                if (action.matches(ACTION_HIDE.toRegex())) {
                    hidePermanent(context, intent.getIntExtra(Constants.INTENT_ID, 0))
                } else if (action.matches(ACTION_RUN.toRegex())) {
                    val id = intent.getIntExtra(Constants.INTENT_ID, 0)
                    var windowType = Prefs.getInstance(context).reminderType
                    val ignore = Prefs.getInstance(context).isIgnoreWindowType
                    val reminder = AppDb.getAppDatabase(context).reminderDao().getById(id)
                    if (!ignore) {
                        if (reminder != null) {
                            windowType = reminder.windowType
                        }
                    }
                    Timber.d("start: ignore -> %b, event -> %s", ignore, reminder)
                    if (windowType == 0) {
                        context.startActivity(ReminderDialogActivity.getLaunchIntent(context, id))
                    } else {
                        ReminderUtils.showSimpleReminder(context, id)
                    }
                } else {
                    showReminder(context, intent.getIntExtra(Constants.INTENT_ID, 0))
                }
            }
        }
    }

    companion object {

        val ACTION_SHOW = Actions.Reminder.ACTION_SHOW_FULL
        val ACTION_HIDE = Actions.Reminder.ACTION_HIDE_SIMPLE
        val ACTION_RUN = Actions.Reminder.ACTION_RUN

        private val TAG = "ReminderActionService"
    }
}
