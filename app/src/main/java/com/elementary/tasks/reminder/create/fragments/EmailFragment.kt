package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.onChanged
import com.elementary.tasks.databinding.FragmentReminderEmailBinding
import timber.log.Timber

class EmailFragment : RepeatableTypeFragment<FragmentReminderEmailBinding>() {

    override fun prepare(): Reminder? {
        val reminder = iFace.state.reminder
        val email = binding.mail.text.toString().trim()
        if (TextUtils.isEmpty(email) || !email.matches(".*@.*..*".toRegex())) {
            iFace.showSnackbar(getString(R.string.email_is_incorrect))
            return null
        }
        val subjectString = binding.subject.text.toString().trim()
        if (TextUtils.isEmpty(subjectString)) {
            iFace.showSnackbar(getString(R.string.you_dont_insert_any_message))
            return null
        }
        val startTime = binding.dateView.dateTime
        Timber.d("EVENT_TIME ${TimeUtil.logTime(startTime)}")

        if (!TimeCount.isCurrent(startTime)) {
            iFace.showSnackbar(getString(R.string.reminder_is_outdated))
            return null
        }

        if (!validBefore(startTime, reminder)) {
            iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
            return null
        }
        val gmtTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.subject = subjectString
        reminder.target = email
        reminder.type = Reminder.BY_DATE_EMAIL
        reminder.eventTime = gmtTime
        reminder.startTime = gmtTime
        reminder.after = 0L
        reminder.dayOfMonth = 0
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
        return reminder
    }

    override fun layoutRes(): Int = R.layout.fragment_reminder_email

    override fun provideViews() {
        setViews(
                scrollView = binding.scrollView,
                expansionLayout = binding.moreLayout,
                ledPickerView = binding.ledView,
                calendarCheck = binding.exportToCalendar,
                tasksCheck = binding.exportToTasks,
                extraView = binding.tuneExtraView,
                melodyView = binding.melodyView,
                attachmentView = binding.attachmentView,
                groupView = binding.groupView,
                summaryView = binding.taskSummary,
                beforePickerView = binding.beforeView,
                dateTimeView = binding.dateView,
                loudnessPickerView = binding.loudnessView,
                priorityPickerView = binding.priorityView,
                repeatLimitView = binding.repeatLimitView,
                repeatView = binding.repeatView,
                windowTypeView = binding.windowTypeView
        )
    }

    override fun onNewHeader(newHeader: String) {
        binding.cardSummary?.text = newHeader
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tuneExtraView.hint = getString(R.string.message)
        binding.tuneExtraView.hasAutoExtra = true

        binding.mail.onChanged {
            iFace.state.isEmailOrSubjectChanged = true
            iFace.state.email = it
        }
        binding.subject.onChanged {
            iFace.state.isEmailOrSubjectChanged = true
            iFace.state.subject = it
        }

        editReminder()
    }

    private fun editReminder() {
        if (iFace.state.isEmailOrSubjectChanged) {
            binding.mail.setText(iFace.state.email)
            binding.subject.setText(iFace.state.subject)
        } else {
            binding.mail.setText(iFace.state.reminder.target)
            binding.subject.setText(iFace.state.reminder.subject)
        }
    }
}
