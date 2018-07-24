package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.birthdays.ShowBirthdayActivity
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.*
import java.util.*
import javax.inject.Inject

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
class BirthdayActionService : BaseBroadcast() {

    @Inject
    lateinit var updatesHelper: UpdatesHelper

    init {
        ReminderApp.appComponent.inject(this)
    }

    private fun updateBirthday(context: Context, item: Birthday) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val year = calendar.get(Calendar.YEAR)
        item.showedYear = year
        AppDb.getAppDatabase(context).birthdaysDao().insert(item)
    }

    private fun sendSms(context: Context, intent: Intent) {
        val item = AppDb.getAppDatabase(context).birthdaysDao().getById(intent.getIntExtra(Constants.INTENT_ID, 0))
        if (item != null && Permissions.checkPermission(context, Permissions.SEND_SMS)) {
            TelephonyUtil.sendSms(item.number, context)
            updateBirthday(context, item)
            finish(notifier, updatesHelper, item.uniqueId)
        } else {
            hidePermanent(context, intent.getIntExtra(Constants.INTENT_ID, 0))
        }
    }

    private fun makeCall(context: Context, intent: Intent) {
        val item = AppDb.getAppDatabase(context).birthdaysDao().getById(intent.getIntExtra(Constants.INTENT_ID, 0))
        if (item != null && Permissions.checkPermission(context, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(item.number, context)
            updateBirthday(context, item)
            finish(notifier, updatesHelper, item.uniqueId)
        } else {
            hidePermanent(context, intent.getIntExtra(Constants.INTENT_ID, 0))
        }
    }

    private fun showReminder(context: Context, intent: Intent) {
        val reminder = AppDb.getAppDatabase(context).birthdaysDao().getById(intent.getIntExtra(Constants.INTENT_ID, 0))
        if (reminder != null) {
            val notificationIntent = ShowBirthdayActivity.getLaunchIntent(context,
                    intent.getIntExtra(Constants.INTENT_ID, 0))
            notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
            context.startActivity(notificationIntent)
            notifier.hideNotification(PermanentBirthdayReceiver.BIRTHDAY_PERM_ID)
        }
    }

    private fun hidePermanent(context: Context, id: Int) {
        if (id == 0) return
        val item = AppDb.getAppDatabase(context).birthdaysDao().getById(id)
        if (item != null) {
            updateBirthday(context, item)
            finish(notifier, updatesHelper, item.uniqueId)
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            LogUtil.d(TAG, "onStartCommand: " + action!!)
            when {
                action.matches(ACTION_CALL.toRegex()) -> makeCall(context, intent)
                action.matches(ACTION_SMS.toRegex()) -> sendSms(context, intent)
                action.matches(PermanentBirthdayReceiver.ACTION_HIDE.toRegex()) -> hidePermanent(context, intent.getIntExtra(Constants.INTENT_ID, 0))
                else -> showReminder(context, intent)
            }
        }
    }

    companion object {

        const val ACTION_SHOW = Actions.Birthday.ACTION_SHOW_FULL
        const val ACTION_CALL = Actions.Birthday.ACTION_CALL
        const val ACTION_SMS = Actions.Birthday.ACTION_SMS

        private const val TAG = "BirthdayActionService"

        fun hide(context: Context, id: Int): Intent {
            return intent(context, id, PermanentBirthdayReceiver.ACTION_HIDE)
        }

        fun call(context: Context, id: Int): Intent {
            return intent(context, id, ACTION_CALL)
        }

        fun show(context: Context, id: Int): Intent {
            return intent(context, id, ACTION_SHOW)
        }

        fun sms(context: Context, id: Int): Intent {
            return intent(context, id, ACTION_SMS)
        }

        private fun intent(context: Context, id: Int, action: String): Intent {
            val intent = Intent(context, BirthdayActionService::class.java)
            intent.action = action
            intent.putExtra(Constants.INTENT_ID, id)
            return intent
        }

        private fun finish(notifier: Notifier, updatesHelper: UpdatesHelper, id: Int) {
            notifier.hideNotification( id)
            updatesHelper.updateWidget()
            updatesHelper.updateCalendarWidget()
        }
    }
}
