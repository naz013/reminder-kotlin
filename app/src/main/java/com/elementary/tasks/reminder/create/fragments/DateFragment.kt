package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.ClosableLegacyBuilderWarningView
import com.elementary.tasks.databinding.FragmentReminderDateBinding
import com.github.naz013.logging.Logger

class DateFragment : RepeatableTypeFragment<FragmentReminderDateBinding>() {

  override fun getExplanationVisibilityType(): ReminderExplanationVisibility.Type {
    return ReminderExplanationVisibility.Type.BY_DATE
  }

  override fun getExplanationView(): View {
    return binding.explanationView
  }

  override fun setCloseListenerToExplanationView(listener: View.OnClickListener) {
    binding.explanationView.setOnClickListener(listener)
  }

  override fun prepare(): Reminder? {
    val reminder = iFace.state.reminder
    var type = Reminder.BY_DATE
    val isAction = binding.actionView.hasAction()
    if (TextUtils.isEmpty(reminder.summary) && !isAction) {
      binding.taskLayout.error = string(R.string.task_summary_is_empty)
      binding.taskLayout.isErrorEnabled = true
      return null
    }
    var number = ""
    if (isAction) {
      number = binding.actionView.number
      if (TextUtils.isEmpty(number)) {
        iFace.showSnackbar(string(R.string.you_dont_insert_number))
        return null
      }
      type = if (binding.actionView.actionState == ActionView.ActionState.CALL) {
        Reminder.BY_DATE_CALL
      } else {
        Reminder.BY_DATE_SMS
      }
    }
    Logger.d("prepare: $type")

    val startTime = binding.dateView.selectedDateTime
    Logger.d("EVENT_TIME ${dateTimeManager.logDateTime(startTime)}")
    if (!dateTimeManager.isCurrent(startTime)) {
      iFace.showSnackbar(string(R.string.reminder_is_outdated))
      return null
    }

    if (!validBefore(startTime, reminder)) {
      iFace.showSnackbar(string(R.string.invalid_remind_before_parameter))
      return null
    }

    val gmtTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.target = number
    reminder.type = type
    reminder.eventTime = gmtTime
    reminder.startTime = gmtTime
    reminder.dayOfMonth = 0
    reminder.after = 0L
    reminder.delay = 0
    reminder.eventCount = 0
    reminder.recurDataObject = null
    return reminder
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderDateBinding.inflate(inflater, container, false)

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
      binding.repeatView,
      binding.actionView
    )
  }

  override fun getLegacyMessageView(): ClosableLegacyBuilderWarningView {
    return binding.legacyBuilderWarningView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.tuneExtraView.hasAutoExtra = false
  }

  override fun updateActions() {
    if (binding.actionView.hasAction()) {
      if (binding.actionView.actionState == ActionView.ActionState.SMS) {
        binding.tuneExtraView.hasAutoExtra = false
      } else {
        binding.tuneExtraView.hasAutoExtra = true
        binding.tuneExtraView.hint = string(R.string.enable_making_phone_calls_automatically)
      }
    } else {
      binding.tuneExtraView.hasAutoExtra = false
    }
  }
}
