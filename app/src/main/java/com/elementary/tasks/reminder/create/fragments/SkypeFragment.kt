package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
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
        val reminder = reminderInterface.state.reminder
        if (!SuperUtil.isSkypeClientInstalled(context!!)) {
            showInstallSkypeDialog()
            return null
        }
        if (TextUtils.isEmpty(reminder.summary)) {
            taskLayout.error = getString(R.string.task_summary_is_empty)
            taskLayout.isErrorEnabled = true
            return null
        }
        val number = skypeContact.text.toString().trim()
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
        Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
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

    override fun layoutRes(): Int = R.layout.fragment_reminder_skype

    override fun provideViews() {
        setViews(
                scrollView = scrollView,
                expansionLayout = moreLayout,
                ledPickerView = ledView,
                calendarCheck = exportToCalendar,
                tasksCheck = exportToTasks,
                extraView = tuneExtraView,
                melodyView = melodyView,
                attachmentView = attachmentView,
                groupView = groupView,
                summaryView = taskSummary,
                beforePickerView = beforeView,
                dateTimeView = dateView,
                loudnessPickerView = loudnessView,
                priorityPickerView = priorityView,
                repeatLimitView = repeatLimitView,
                repeatView = repeatView,
                windowTypeView = windowTypeView
        )
    }

    override fun onNewHeader(newHeader: String) {
        cardSummary?.text = newHeader
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tuneExtraView.hasAutoExtra = false
        editReminder()
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
        val reminder = reminderInterface.state.reminder
        showGroup(groupView, reminder)
        when (reminder.type) {
            Reminder.BY_SKYPE_CALL -> skypeCall.isChecked = true
            Reminder.BY_SKYPE_VIDEO -> skypeVideo.isChecked = true
            Reminder.BY_SKYPE -> skypeChat.isChecked = true
        }
        if (reminder.target != "") {
            skypeContact.setText(reminder.target)
        }
    }
}
