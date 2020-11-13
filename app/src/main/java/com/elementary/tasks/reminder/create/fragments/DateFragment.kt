package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.databinding.FragmentReminderDateBinding
import timber.log.Timber

class DateFragment : RepeatableTypeFragment<FragmentReminderDateBinding>() {

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
      type = if (binding.actionView.type == ActionView.TYPE_CALL) {
        Reminder.BY_DATE_CALL
      } else {
        Reminder.BY_DATE_SMS
      }
    }
    Timber.d("prepare: $type")

    val startTime = binding.dateView.dateTime
    Timber.d("EVENT_TIME ${TimeUtil.logTime(startTime)}")
    if (!TimeCount.isCurrent(startTime)) {
      iFace.showSnackbar(string(R.string.reminder_is_outdated))
      return null
    }

    if (!validBefore(startTime, reminder)) {
      iFace.showSnackbar(string(R.string.invalid_remind_before_parameter))
      return null
    }

    val gmtTime = TimeUtil.getGmtFromDateTime(startTime)
    reminder.target = number
    reminder.type = type
    reminder.eventTime = gmtTime
    reminder.startTime = gmtTime
    reminder.dayOfMonth = 0
    reminder.after = 0L
    reminder.delay = 0
    reminder.eventCount = 0
    return reminder
  }

  override fun layoutRes(): Int = R.layout.fragment_reminder_date

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
      windowTypeView = binding.windowTypeView,
      actionView = binding.actionView,
      calendarPicker = binding.calendarPicker
    )
  }

  override fun onNewHeader(newHeader: String) {
    binding.cardSummary.text = newHeader
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.tuneExtraView.hasAutoExtra = false
  }

  override fun updateActions() {
    if (binding.actionView.hasAction()) {
      if (binding.actionView.type == ActionView.TYPE_MESSAGE) {
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
