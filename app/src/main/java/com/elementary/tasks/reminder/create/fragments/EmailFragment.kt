package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import kotlinx.android.synthetic.main.fragment_reminder_email.*
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
class EmailFragment : RepeatableTypeFragment() {

    override fun prepare(): Reminder? {
        val reminder = reminderInterface.state.reminder
        val email = mail.text.toString().trim()
        if (TextUtils.isEmpty(email) || !email.matches(".*@.*..*".toRegex())) {
            reminderInterface.showSnackbar(getString(R.string.email_is_incorrect))
            return null
        }
        val subjectString = subject.text.toString().trim()
        if (TextUtils.isEmpty(subjectString)) {
            reminderInterface.showSnackbar(getString(R.string.you_dont_insert_any_message))
            return null
        }
        val startTime = dateView.dateTime
        if (reminder.remindBefore > 0 && startTime - reminder.remindBefore < System.currentTimeMillis()) {
            reminderInterface.showSnackbar(getString(R.string.invalid_remind_before_parameter))
            return null
        }
        reminder.subject = subjectString
        reminder.target = email

        reminder.type = Reminder.BY_DATE_EMAIL
        reminder.startTime = reminder.eventTime
        Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            reminderInterface.showSnackbar(getString(R.string.reminder_is_outdated))
            return null
        }
        return reminder
    }

    override fun layoutRes(): Int = R.layout.fragment_reminder_email

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
        tuneExtraView.hint = getString(R.string.message)
        tuneExtraView.hasAutoExtra = true
        editReminder()
    }

    private fun editReminder() {
        val reminder = reminderInterface.state.reminder
        showGroup(groupView, reminder)
        mail.setText(reminder.target)
        subject.setText(reminder.subject)
    }
}
