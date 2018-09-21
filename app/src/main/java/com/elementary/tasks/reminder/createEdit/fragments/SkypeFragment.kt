package com.elementary.tasks.reminder.createEdit.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import kotlinx.android.synthetic.main.fragment_reminder_skype.*

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

    override fun prepare(): Reminder? {
        val iFace = reminderInterface ?: return null
        if (!SuperUtil.isSkypeClientInstalled(context!!)) {
            showInstallSkypeDialog()
            return null
        }
//        if (TextUtils.isEmpty(iFace.summary)) {
//            iFace.showSnackbar(getString(R.string.task_summary_is_empty))
//            return null
//        }
        val number = skypeContact.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(number)) {
            iFace.showSnackbar(getString(R.string.you_dont_insert_number))
            return null
        }
        var reminder = iFace.reminder
        val type = getType(skypeGroup.checkedRadioButtonId)
        val startTime = dateView.dateTime
        val before = before_view.beforeValue
        if (before > 0 && startTime - before < System.currentTimeMillis()) {
            Toast.makeText(context, R.string.invalid_remind_before_parameter, Toast.LENGTH_SHORT).show()
            return null
        }
        if (reminder == null) {
            reminder = Reminder()
        }
        reminder.target = number
        reminder.type = type
        val repeat = repeatView.repeat
        reminder.repeatInterval = repeat
        reminder.exportToCalendar = exportToCalendar.isChecked
        reminder.exportToTasks = exportToTasks.isChecked
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
        val builder = dialogues.getDialog(context!!)
        builder.setMessage(R.string.skype_is_not_installed)
        builder.setPositiveButton(R.string.yes) { dialogInterface, _ ->
            dialogInterface.dismiss()
            SuperUtil.installSkype(context!!)
        }
        builder.setNegativeButton(R.string.cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.create().show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_date_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
//            R.id.action_limit -> changeLimit()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminder_skype, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skypeChat.setOnCheckedChangeListener { _, b ->
//            if (b) {
//                reminderInterface?.setEventHint(getString(R.string.message))
//            } else {
//                reminderInterface?.setEventHint(getString(R.string.remind_me))
//            }
        }
        initScreenState()
        editReminder()
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
        val reminder = reminderInterface.reminder
        exportToCalendar.isChecked = reminder.exportToCalendar
        exportToTasks.isChecked = reminder.exportToTasks
        dateView.setDateTime(reminder.eventTime)
        repeatView.repeat = reminder.repeatInterval
        before_view.setBefore(reminder.remindBefore)
        val type = reminder.type
        when (type) {
            Reminder.BY_SKYPE_CALL -> skypeCall.isChecked = true
            Reminder.BY_SKYPE_VIDEO -> skypeVideo.isChecked = true
            Reminder.BY_SKYPE -> skypeChat.isChecked = true
        }
        if (reminder.target != "") {
            skypeContact.setText(reminder.target)
        }
    }

    companion object {

        private const val TAG = "DateFragment"
    }
}
