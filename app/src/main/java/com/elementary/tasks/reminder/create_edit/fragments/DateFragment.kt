package com.elementary.tasks.reminder.create_edit.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.databinding.FragmentDateBinding
import com.elementary.tasks.core.data.models.Reminder

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

    private var binding: FragmentDateBinding? = null
    private val mActionListener = object : ActionView.OnActionListener {
        override fun onActionChange(hasAction: Boolean) {
            if (!hasAction) {
                `interface`!!.setEventHint(getString(R.string.remind_me))
                `interface`!!.setHasAutoExtra(false, null)
            }
        }

        override fun onTypeChange(isMessageType: Boolean) {
            if (isMessageType) {
                `interface`!!.setEventHint(getString(R.string.message))
                `interface`!!.setHasAutoExtra(true, getString(R.string.enable_sending_sms_automatically))
            } else {
                `interface`!!.setEventHint(getString(R.string.remind_me))
                `interface`!!.setHasAutoExtra(true, getString(R.string.enable_making_phone_calls_automatically))
            }
        }
    }

    override fun prepare(): Reminder? {
        if (`interface` == null) return null
        var reminder: Reminder? = `interface`!!.reminder
        var type = Reminder.BY_DATE
        val isAction = binding!!.actionView.hasAction()
        if (TextUtils.isEmpty(`interface`!!.summary) && !isAction) {
            `interface`!!.showSnackbar(getString(R.string.task_summary_is_empty))
            return null
        }
        var number: String? = null
        if (isAction) {
            number = binding!!.actionView.number
            if (TextUtils.isEmpty(number)) {
                `interface`!!.showSnackbar(getString(R.string.you_dont_insert_number))
                return null
            }
            if (binding!!.actionView.type == ActionView.TYPE_CALL) {
                type = Reminder.BY_DATE_CALL
            } else {
                type = Reminder.BY_DATE_SMS
            }
        }
        val startTime = binding!!.dateView.dateTime
        val before = binding!!.beforeView.beforeValue
        if (before > 0 && startTime - before < System.currentTimeMillis()) {
            Toast.makeText(context, R.string.invalid_remind_before_parameter, Toast.LENGTH_SHORT).show()
            return null
        }
        if (reminder == null) {
            reminder = Reminder()
        }
        reminder.target = number
        reminder.type = type
        reminder.repeatInterval = binding!!.repeatView.repeat
        reminder.isExportToCalendar = binding!!.exportToCalendar.isChecked
        reminder.isExportToTasks = binding!!.exportToTasks.isChecked
        reminder.setClear(`interface`)
        reminder.remindBefore = before
        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
        LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            Toast.makeText(context, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show()
            return null
        }
        return reminder
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.fragment_date_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_limit -> changeLimit()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDateBinding.inflate(inflater, container, false)
        binding!!.repeatView.enablePrediction(true)
        binding!!.dateView.setEventListener(binding!!.repeatView.eventListener)
        binding!!.actionView.setListener(mActionListener)
        binding!!.actionView.setActivity(activity)
        binding!!.actionView.setContactClickListener { view -> selectContact() }
        if (`interface`!!.isExportToCalendar) {
            binding!!.exportToCalendar.visibility = View.VISIBLE
        } else {
            binding!!.exportToCalendar.visibility = View.GONE
        }
        if (`interface`!!.isExportToTasks) {
            binding!!.exportToTasks.visibility = View.VISIBLE
        } else {
            binding!!.exportToTasks.visibility = View.GONE
        }
        editReminder()
        return binding!!.root
    }

    private fun editReminder() {
        if (`interface`!!.reminder == null) return
        val reminder = `interface`!!.reminder
        binding!!.exportToCalendar.isChecked = reminder.isExportToCalendar
        binding!!.exportToTasks.isChecked = reminder.isExportToTasks
        binding!!.dateView.setDateTime(reminder.eventTime)
        binding!!.repeatView.setDateTime(reminder.eventTime)
        binding!!.repeatView.repeat = reminder.repeatInterval
        binding!!.beforeView.setBefore(reminder.remindBefore)
        if (reminder.target != null) {
            binding!!.actionView.setAction(true)
            binding!!.actionView.number = reminder.target
            if (Reminder.isKind(reminder.type, Reminder.Kind.CALL)) {
                binding!!.actionView.type = ActionView.TYPE_CALL
            } else if (Reminder.isKind(reminder.type, Reminder.Kind.SMS)) {
                binding!!.actionView.type = ActionView.TYPE_MESSAGE
            }
        }
    }

    private fun selectContact() {
        if (Permissions.checkPermission(activity, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            SuperUtil.selectContact(activity!!, Constants.REQUEST_CODE_CONTACTS)
        } else {
            Permissions.requestPermission(activity, CONTACTS, Permissions.READ_CONTACTS, Permissions.READ_CALLS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS && resultCode == Activity.RESULT_OK) {
            val number = data!!.getStringExtra(Constants.SELECTED_CONTACT_NUMBER)
            binding!!.actionView.number = number
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        binding!!.actionView.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size == 0) return
        when (requestCode) {
            CONTACTS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectContact()
            }
        }
    }

    companion object {

        private val TAG = "DateFragment"
        private val CONTACTS = 112
    }
}
