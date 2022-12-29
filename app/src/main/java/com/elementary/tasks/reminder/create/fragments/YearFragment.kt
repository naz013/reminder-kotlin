package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.minusMillis
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.DateTimeView
import com.elementary.tasks.databinding.FragmentReminderYearBinding
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber

class YearFragment : RepeatableTypeFragment<FragmentReminderYearBinding>() {

  override fun prepare(): Reminder? {
    val reminder = iFace.state.reminder
    var type = Reminder.BY_DAY_OF_YEAR
    val isAction = binding.actionView.hasAction()
    if (TextUtils.isEmpty(reminder.summary) && !isAction) {
      binding.taskLayout.error = getString(R.string.task_summary_is_empty)
      binding.taskLayout.isErrorEnabled = true
      return null
    }
    var number = ""
    if (isAction) {
      number = binding.actionView.number
      if (TextUtils.isEmpty(number)) {
        iFace.showSnackbar(getString(R.string.you_dont_insert_number))
        return null
      }
      type = if (binding.actionView.type == ActionView.TYPE_CALL) {
        Reminder.BY_DAY_OF_YEAR_CALL
      } else {
        Reminder.BY_DAY_OF_YEAR_SMS
      }
    }
    reminder.weekdays = listOf()
    reminder.target = number
    reminder.type = type
    reminder.dayOfMonth = iFace.state.day
    reminder.monthOfYear = iFace.state.month
    reminder.after = 0L
    reminder.delay = 0
    reminder.eventCount = 0
    reminder.repeatInterval = 0

    reminder.eventTime = dateTimeManager.getGmtFromDateTime(getDateTime())
    val startTime = dateTimeManager.getNextYearDayTime(reminder)

    if (reminder.remindBefore > 0 &&
      !dateTimeManager.isCurrent(startTime.minusMillis(reminder.remindBefore))
    ) {
      iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
      return null
    }

    reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(startTime)
    Timber.d("EVENT_TIME %s", dateTimeManager.logDateTime(startTime))
    if (!dateTimeManager.isCurrent(reminder.eventTime)) {
      iFace.showSnackbar(getString(R.string.reminder_is_outdated))
      return null
    }
    return reminder
  }

  private fun getDateTime(): LocalDateTime {
    return LocalDateTime.of(LocalDate.now(), iFace.state.time.withSecond(0))
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderYearBinding.inflate(inflater, container, false)

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

    binding.dateView.setDateFormat(dateTimeManager.simpleDateFormatter())
    binding.dateView.setOnSelectListener(object : DateTimeView.OnSelectListener {
      override fun onDateSelect(date: LocalDate) {
        iFace.state.date = date
      }

      override fun onTimeSelect(time: LocalTime) {
        iFace.state.time = time
      }
    })

    binding.dateView.selectedDateTime = LocalDateTime.of(iFace.state.date, iFace.state.time)
    editReminder()
  }

  override fun updateActions() {
    if (binding.actionView.hasAction()) {
      if (binding.actionView.type == ActionView.TYPE_MESSAGE) {
        binding.tuneExtraView.hasAutoExtra = false
      } else {
        binding.tuneExtraView.hasAutoExtra = true
        binding.tuneExtraView.hint = getString(R.string.enable_making_phone_calls_automatically)
      }
    } else {
      binding.tuneExtraView.hasAutoExtra = false
    }
  }

  private fun updateDateTime(reminder: Reminder) {
    val dateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: LocalDateTime.now()
    binding.dateView.selectedDateTime = dateTime
    iFace.state.date = dateTime.toLocalDate()
    iFace.state.time = dateTime.toLocalTime()
  }

  private fun editReminder() {
    updateDateTime(iFace.state.reminder)
  }
}
