package com.elementary.tasks.reminder.create_edit.fragments

import android.app.AlertDialog
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
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.databinding.FragmentReminderSkypeBinding
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

class SkypeFragment : RepeatableTypeFragment() {

    private var binding: FragmentReminderSkypeBinding? = null

    override fun prepare(): Reminder? {
        if (`interface` == null) return null
        if (!SuperUtil.isSkypeClientInstalled(context!!)) {
            showInstallSkypeDialog()
            return null
        }
        if (TextUtils.isEmpty(`interface`!!.summary)) {
            `interface`!!.showSnackbar(getString(R.string.task_summary_is_empty))
            return null
        }
        val number = binding!!.skypeContact.text!!.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(number)) {
            `interface`!!.showSnackbar(getString(R.string.you_dont_insert_number))
            return null
        }
        var reminder: Reminder? = `interface`!!.reminder
        val type = getType(binding!!.skypeGroup.checkedRadioButtonId)
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

    private fun showInstallSkypeDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setMessage(R.string.skype_is_not_installed)
        builder.setPositiveButton(R.string.yes) { dialogInterface, i ->
            dialogInterface.dismiss()
            SuperUtil.installSkype(context!!)
        }
        builder.setNegativeButton(R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.create().show()
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
        binding = FragmentReminderSkypeBinding.inflate(inflater, container, false)
        binding!!.repeatView.enablePrediction(true)
        binding!!.dateView.setEventListener(binding!!.repeatView.eventListener)
        binding!!.skypeChat.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                `interface`!!.setEventHint(getString(R.string.message))
            } else {
                `interface`!!.setEventHint(getString(R.string.remind_me))
            }
        }
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

    private fun getType(checkedId: Int): Int {
        var type = Reminder.BY_SKYPE_CALL
        when (checkedId) {
            R.id.skypeCall -> type = Reminder.BY_SKYPE_CALL
            R.id.skypeChat -> type = Reminder.BY_SKYPE
            R.id.skypeVideo -> type = Reminder.BY_SKYPE_VIDEO
        }
        return type
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
        val type = reminder.type
        if (type == Reminder.BY_SKYPE_CALL)
            binding!!.skypeCall.isChecked = true
        else if (type == Reminder.BY_SKYPE_VIDEO)
            binding!!.skypeVideo.isChecked = true
        else if (type == Reminder.BY_SKYPE) binding!!.skypeChat.isChecked = true
        if (reminder.target != null) {
            binding!!.skypeContact.setText(reminder.target)
        }
    }

    companion object {

        private val TAG = "DateFragment"
    }
}
