package com.elementary.tasks.reminder.createEdit.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.views.ActionView
import kotlinx.android.synthetic.main.fragment_reminder_date.*

/**
 * Copyright 2016 Nazar Suhovich
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
class DateFragment : RepeatableTypeFragment() {

    private val mActionListener = object : ActionView.OnActionListener {
        override fun onActionChange(hasAction: Boolean) {
            reminderInterface.hasAutoExtra = hasAction
            if (!hasAction) {
                reminderInterface.autoExtraHint = ""
            }
        }

        override fun onTypeChange(isMessageType: Boolean) {
            reminderInterface.hasAutoExtra = true
            if (isMessageType) {
                reminderInterface.autoExtraHint = getString(R.string.enable_sending_sms_automatically)
            } else {
                reminderInterface.autoExtraHint = getString(R.string.enable_making_phone_calls_automatically)
            }
        }
    }

    override fun prepare(): Reminder? {
        val iFace = reminderInterface
        val reminder = iFace.reminder
        var type = Reminder.BY_DATE
        val isAction = actionView.hasAction()
        if (TextUtils.isEmpty(iFace.reminder.summary) && !isAction) {
            iFace.showSnackbar(getString(R.string.task_summary_is_empty))
            return null
        }
        var number = ""
        if (isAction) {
            number = actionView.number
            if (TextUtils.isEmpty(number)) {
                iFace.showSnackbar(getString(R.string.you_dont_insert_number))
                return null
            }
            type = if (actionView.type == ActionView.TYPE_CALL) {
                Reminder.BY_DATE_CALL
            } else {
                Reminder.BY_DATE_SMS
            }
        }
        val startTime = dateView.dateTime
        if (reminder.remindBefore > 0 && startTime - reminder.remindBefore < System.currentTimeMillis()) {
            Toast.makeText(context, R.string.invalid_remind_before_parameter, Toast.LENGTH_SHORT).show()
            return null
        }
        reminder.target = number
        reminder.type = type
        reminder.startTime = reminder.eventTime
        LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            Toast.makeText(context, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show()
            return null
        }
        return reminder
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminder_date, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repeatView.enablePrediction(true)
        dateView.setEventListener(repeatView.eventListener)
        actionView.setListener(mActionListener)
        actionView.setActivity(activity!!)
        actionView.setContactClickListener(View.OnClickListener { selectContact() })
        initScreenState()
        initPropertyFields()
        editReminder()
    }

    private fun initPropertyFields() {
        taskSummary.bindProperty(reminderInterface.reminder.summary) {
            reminderInterface.reminder.summary = it.trim()
        }
        before_view.bindProperty(reminderInterface.reminder.remindBefore) {
            reminderInterface.reminder.remindBefore = it
        }
        repeatView.bindProperty(reminderInterface.reminder.repeatInterval) {
            reminderInterface.reminder.repeatInterval = it
        }
        exportToCalendar.bindProperty(reminderInterface.reminder.exportToCalendar) {
            reminderInterface.reminder.exportToCalendar = it
        }
        exportToTasks.bindProperty(reminderInterface.reminder.exportToTasks) {
            reminderInterface.reminder.exportToTasks = it
        }
        dateView.bindProperty(reminderInterface.reminder.eventTime) {
            reminderInterface.reminder.eventTime = it
        }
    }

    private fun initScreenState() {
        if (reminderInterface.canExportToCalendar) {
            exportToCalendar.visibility = View.VISIBLE
        } else {
            exportToCalendar.visibility = View.GONE
        }
        if (reminderInterface.canExportToTasks) {
            exportToTasks.visibility = View.VISIBLE
        } else {
            exportToTasks.visibility = View.GONE
        }
    }

    private fun editReminder() {
        val reminder = reminderInterface.reminder
        repeatView.setDateTime(reminder.eventTime)
        if (reminder.target != "") {
            actionView.setAction(true)
            actionView.number = reminder.target
            if (Reminder.isKind(reminder.type, Reminder.Kind.CALL)) {
                actionView.type = ActionView.TYPE_CALL
            } else if (Reminder.isKind(reminder.type, Reminder.Kind.SMS)) {
                actionView.type = ActionView.TYPE_MESSAGE
            }
        }
    }

    private fun selectContact() {
        if (Permissions.checkPermission(activity!!, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            SuperUtil.selectContact(activity!!, Constants.REQUEST_CODE_CONTACTS)
        } else {
            Permissions.requestPermission(activity!!, CONTACTS, Permissions.READ_CONTACTS, Permissions.READ_CALLS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS && resultCode == Activity.RESULT_OK) {
            val number = data?.getStringExtra(Constants.SELECTED_CONTACT_NUMBER) ?: ""
            actionView.number = number
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        actionView.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            CONTACTS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectContact()
            }
        }
    }

    companion object {

        private const val TAG = "DateFragment"
        private const val CONTACTS = 112
    }
}
