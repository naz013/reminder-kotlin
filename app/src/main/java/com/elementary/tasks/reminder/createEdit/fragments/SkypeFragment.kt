package com.elementary.tasks.reminder.createEdit.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.*
import kotlinx.android.synthetic.main.fragment_reminder_skype.*
import timber.log.Timber

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
        val reminder = reminderInterface.reminder
        if (!SuperUtil.isSkypeClientInstalled(context!!)) {
            showInstallSkypeDialog()
            return null
        }
        if (TextUtils.isEmpty(reminder.summary)) {
            taskLayout.error = getString(R.string.task_summary_is_empty)
            taskLayout.isErrorEnabled = true
            return null
        }
        val number = skypeContact.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(number)) {
            reminderInterface.showSnackbar(getString(R.string.you_dont_insert_number))
            return null
        }
        val type = getType(skypeGroup.checkedRadioButtonId)
        val startTime = dateView.dateTime
        if (reminder.remindBefore > 0 && startTime - reminder.remindBefore < System.currentTimeMillis()) {
            reminderInterface.showSnackbar(getString(R.string.invalid_remind_before_parameter))
            return null
        }
        reminder.target = number
        reminder.type = type
        reminder.startTime = reminder.eventTime
        Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            reminderInterface.showSnackbar(getString(R.string.reminder_is_outdated))
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminder_skype, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollView(scrollView) {
            reminderInterface.updateScroll(it)
        }
        moreLayout.isNestedScrollingEnabled = false

        if (Module.isPro) {
            ledView.visibility = View.VISIBLE
        } else {
            ledView.visibility = View.GONE
        }

        tuneExtraView.dialogues = dialogues
        tuneExtraView.hasAutoExtra = false

        melodyView.onFileSelectListener = {
            reminderInterface.selectMelody()
        }
        attachmentView.onFileSelectListener = {
            reminderInterface.attachFile()
        }
        groupView.onGroupSelectListener = {
            reminderInterface.selectGroup()
        }

        initScreenState()
        initPropertyFields()
        editReminder()
    }

    private fun initPropertyFields() {
        taskSummary.bindProperty(reminderInterface.reminder.summary) {
            reminderInterface.reminder.summary = it.trim()
        }
        beforeView.bindProperty(reminderInterface.reminder.remindBefore) {
            reminderInterface.reminder.remindBefore = it
            updateHeader()
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
        priorityView.bindProperty(reminderInterface.reminder.priority) {
            reminderInterface.reminder.priority = it
            updateHeader()
        }
        melodyView.bindProperty(reminderInterface.reminder.melodyPath) {
            reminderInterface.reminder.melodyPath = it
        }
        attachmentView.bindProperty(reminderInterface.reminder.attachmentFile) {
            reminderInterface.reminder.attachmentFile = it
        }
        loudnessView.bindProperty(reminderInterface.reminder.volume) {
            reminderInterface.reminder.volume = it
        }
        repeatLimitView.bindProperty(reminderInterface.reminder.repeatLimit) {
            reminderInterface.reminder.repeatLimit = it
        }
        windowTypeView.bindProperty(reminderInterface.reminder.windowType) {
            reminderInterface.reminder.windowType = it
        }
        tuneExtraView.bindProperty(reminderInterface.reminder) {
            reminderInterface.reminder.copyExtra(it)
        }
        if (Module.isPro) {
            ledView.bindProperty(reminderInterface.reminder.color) {
                reminderInterface.reminder.color = it
            }
        }
    }

    private fun updateHeader() {
        cardSummary.text = getSummary()
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
        groupView.reminderGroup = ReminderGroup().apply {
            this.groupColor = reminder.groupColor
            this.groupTitle = reminder.groupTitle
            this.groupUuId = reminder.groupUuId
        }
        when (reminder.type) {
            Reminder.BY_SKYPE_CALL -> skypeCall.isChecked = true
            Reminder.BY_SKYPE_VIDEO -> skypeVideo.isChecked = true
            Reminder.BY_SKYPE -> skypeChat.isChecked = true
        }
        if (reminder.target != "") {
            skypeContact.setText(reminder.target)
        }
    }

    override fun onGroupUpdate(reminderGroup: ReminderGroup) {
        super.onGroupUpdate(reminderGroup)
        groupView.reminderGroup = reminderGroup
        updateHeader()
    }

    override fun onMelodySelect(path: String) {
        super.onMelodySelect(path)
        melodyView.file = path
    }

    override fun onAttachmentSelect(path: String) {
        super.onAttachmentSelect(path)
        attachmentView.file = path
    }
}
