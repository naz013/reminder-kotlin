package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions
import com.elementary.tasks.birthdays.preview.ShowBirthdayActivity
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TelephonyUtil
import timber.log.Timber
import java.util.*

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
class BirthdayActionReceiver : BaseBroadcast() {

    private fun updateBirthday(context: Context, item: Birthday) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val year = calendar.get(Calendar.YEAR)
        item.showedYear = year
        AppDb.getAppDatabase(context).birthdaysDao().insert(item)
    }

    private fun sendSms(context: Context, intent: Intent) {
        val item = AppDb.getAppDatabase(context).birthdaysDao().getById(intent.getStringExtra(Constants.INTENT_ID) ?: "")
        if (item != null) {
            TelephonyUtil.sendSms(item.number, context)
            updateBirthday(context, item)
            finish(context, notifier, item.uniqueId)
        } else {
            hidePermanent(context, intent.getStringExtra(Constants.INTENT_ID) ?: "")
        }
    }

    private fun makeCall(context: Context, intent: Intent) {
        val item = AppDb.getAppDatabase(context).birthdaysDao().getById(intent.getStringExtra(Constants.INTENT_ID) ?: "")
        if (item != null && Permissions.checkPermission(context, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(item.number, context)
            updateBirthday(context, item)
            finish(context, notifier, item.uniqueId)
        } else {
            hidePermanent(context, intent.getStringExtra(Constants.INTENT_ID) ?: "")
        }
    }

    private fun showReminder(context: Context, intent: Intent) {
        val reminder = AppDb.getAppDatabase(context).birthdaysDao().getById(intent.getStringExtra(Constants.INTENT_ID) ?: "")
        if (reminder != null) {
            val notificationIntent = ShowBirthdayActivity.getLaunchIntent(context,
                    intent.getStringExtra(Constants.INTENT_ID) ?: "")
            notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
            context.startActivity(notificationIntent)
            notifier.hideNotification(PermanentBirthdayReceiver.BIRTHDAY_PERM_ID)
        }
    }

    private fun hidePermanent(context: Context, id: String) {
        if (id.isEmpty()) return
        val item = AppDb.getAppDatabase(context).birthdaysDao().getById(id)
        if (item != null) {
            updateBirthday(context, item)
            finish(context, notifier, item.uniqueId)
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            Timber.d("onReceive: $action")
            if (action != null) {
                when {
                    action.matches(ACTION_CALL.toRegex()) -> makeCall(context, intent)
                    action.matches(ACTION_SMS.toRegex()) -> sendSms(context, intent)
                    action.matches(PermanentBirthdayReceiver.ACTION_HIDE.toRegex()) -> {
                        hidePermanent(context, intent.getStringExtra(Constants.INTENT_ID) ?: "")
                    }
                    else -> showReminder(context, intent)
                }
            }
        }
    }

    companion object {
        private const val ACTION_SHOW = Actions.Birthday.ACTION_SHOW_FULL
        private const val ACTION_CALL = Actions.Birthday.ACTION_CALL
        private const val ACTION_SMS = Actions.Birthday.ACTION_SMS

        fun hide(context: Context, id: String): Intent {
            return intent(context, id, PermanentBirthdayReceiver.ACTION_HIDE)
        }

        fun call(context: Context, id: String): Intent {
            return intent(context, id, ACTION_CALL)
        }

        fun show(context: Context, id: String): Intent {
            return intent(context, id, ACTION_SHOW)
        }

        fun sms(context: Context, id: String): Intent {
            return intent(context, id, ACTION_SMS)
        }

        private fun intent(context: Context, id: String, action: String): Intent {
            val intent = Intent(context, BirthdayActionReceiver::class.java)
            intent.action = action
            intent.putExtra(Constants.INTENT_ID, id)
            return intent
        }

        private fun finish(context: Context, notifier: Notifier, id: Int) {
            notifier.hideNotification( id)
            UpdatesHelper.updateWidget(context)
            UpdatesHelper.updateCalendarWidget(context)
        }
    }
}
