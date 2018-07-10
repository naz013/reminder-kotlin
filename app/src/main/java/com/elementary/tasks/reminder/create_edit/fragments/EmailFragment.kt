package com.elementary.tasks.reminder.create_edit.fragments

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
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.databinding.FragmentReminderEmailBinding
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

class EmailFragment : RepeatableTypeFragment() {

    private var binding: FragmentReminderEmailBinding? = null

    override fun prepare(): Reminder? {
        if (`interface` == null) return null
        var reminder: Reminder? = `interface`!!.reminder
        val type = Reminder.BY_DATE_EMAIL
        val email = binding!!.mail.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(email) || !email.matches(".*@.*..*".toRegex())) {
            `interface`!!.showSnackbar(getString(R.string.email_is_incorrect))
            return null
        }
        val subjectString = binding!!.subject.text!!.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(subjectString)) {
            `interface`!!.showSnackbar(getString(R.string.you_dont_insert_any_message))
            return null
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
        reminder.subject = subjectString
        reminder.summary = `interface`!!.summary
        reminder.target = email
        reminder.type = type
        val repeat = binding!!.repeatView.repeat
        reminder.repeatInterval = repeat
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
        binding = FragmentReminderEmailBinding.inflate(inflater, container, false)
        binding!!.repeatView.enablePrediction(true)
        binding!!.dateView.setEventListener(binding!!.repeatView.eventListener)
        `interface`!!.setEventHint(getString(R.string.message))
        `interface`!!.setHasAutoExtra(true, getString(R.string.enable_sending_email_automatically))
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
        binding!!.mail.setText(reminder.target)
        binding!!.subject.setText(reminder.subject)
        binding!!.beforeView.setBefore(reminder.remindBefore)
    }

    companion object {

        private val TAG = "DateFragment"
    }
}
