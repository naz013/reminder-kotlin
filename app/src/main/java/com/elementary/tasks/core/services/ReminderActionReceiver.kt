package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.reminder.preview.ReminderDialogActivity
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
class ReminderActionReceiver : BaseBroadcast() {

    private fun showReminder(context: Context, id: String) {
        val reminder = AppDb.getAppDatabase(context).reminderDao().getById(id) ?: return
        val notificationIntent = ReminderDialogActivity.getLaunchIntent(context, id)
        notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
        context.startActivity(notificationIntent)
        endService(context, reminder.uniqueId)
    }

    private fun hidePermanent(context: Context, id: String) {
        val reminder = AppDb.getAppDatabase(context).reminderDao().getById(id) ?: return
        EventControlFactory.getController(reminder).next()
        endService(context, reminder.uniqueId)
    }

    private fun endService(context: Context, id: Int) {
        Notifier.getManager(context)?.cancel(id)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            Timber.d("onReceive: $action")
            if (action != null) {
                when {
                    action.matches(ACTION_HIDE.toRegex()) -> hidePermanent(context, intent.getStringExtra(Constants.INTENT_ID) ?: "")
                    action.matches(ACTION_RUN.toRegex()) -> {
                        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
                        resolveAction(context, id)
                    }
                    else -> showReminder(context, intent.getStringExtra(Constants.INTENT_ID) ?: "")
                }
            }
        }
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
                        EventJobService.enableDelay(delayTime, id)
                    }
                }
            } else {
                withUIContext {
                    if (windowType == 0) {
                        context.startActivity(ReminderDialogActivity.getLaunchIntent(context, id))
                    } else {
                        ReminderUtils.showSimpleReminder(context, prefs, id)
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_SHOW = Actions.Reminder.ACTION_SHOW_FULL
        const val ACTION_HIDE = Actions.Reminder.ACTION_HIDE_SIMPLE
        const val ACTION_RUN = Actions.Reminder.ACTION_RUN
    }
}
