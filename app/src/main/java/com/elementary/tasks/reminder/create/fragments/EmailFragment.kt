package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.onChanged
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.views.ClosableLegacyBuilderWarningView
import com.elementary.tasks.databinding.FragmentReminderEmailBinding
import timber.log.Timber

class EmailFragment : RepeatableTypeFragment<FragmentReminderEmailBinding>() {

  override fun getExplanationVisibilityType(): ReminderExplanationVisibility.Type {
    return ReminderExplanationVisibility.Type.EMAIL
  }

  override fun getExplanationView(): View {
    return binding.explanationView
  }

  override fun setCloseListenerToExplanationView(listener: View.OnClickListener) {
    binding.explanationView.setOnClickListener(listener)
  }

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
    val startTime = binding.dateView.selectedDateTime
    Timber.d("EVENT_TIME ${dateTimeManager.logDateTime(startTime)}")

    if (!dateTimeManager.isCurrent(startTime)) {
      iFace.showSnackbar(getString(R.string.reminder_is_outdated))
      return null
    }

    if (!validBefore(startTime, reminder)) {
      iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
      return null
    }
    val gmtTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.subject = subjectString
    reminder.target = email
    reminder.type = Reminder.BY_DATE_EMAIL
    reminder.eventTime = gmtTime
    reminder.startTime = gmtTime
    reminder.after = 0L
    reminder.dayOfMonth = 0
    reminder.delay = 0
    reminder.eventCount = 0
    reminder.recurDataObject = null
    return reminder
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderEmailBinding.inflate(inflater, container, false)

  override fun getDynamicViews(): List<View> {
    return listOfNotNull(
      binding.ledView,
      binding.exportToCalendar,
      binding.exportToTasks,
      binding.tuneExtraView,
      binding.attachmentView,
      binding.groupView,
      binding.taskSummary,
      binding.beforeView,
      binding.dateView,
      binding.priorityView,
      binding.repeatLimitView,
      binding.repeatView
    )
  }

  override fun getLegacyMessageView(): ClosableLegacyBuilderWarningView {
    return binding.legacyBuilderWarningView
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
