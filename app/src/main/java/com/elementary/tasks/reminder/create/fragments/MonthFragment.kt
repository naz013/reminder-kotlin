package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.minusMillis
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.databinding.FragmentReminderMonthBinding
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber

class MonthFragment : RepeatableTypeFragment<FragmentReminderMonthBinding>() {

  private val time: LocalTime
    get() = LocalTime.of(iFace.state.hour, iFace.state.minute)

  override fun getExplanationVisibilityType(): ReminderExplanationVisibility.Type {
    return ReminderExplanationVisibility.Type.BY_MONTH
  }

  override fun getExplanationView(): View {
    return binding.explanationView
  }

  override fun setCloseListenerToExplanationView(listener: View.OnClickListener) {
    binding.explanationView.setOnClickListener(listener)
  }

  override fun prepare(): Reminder? {
    val reminder = iFace.state.reminder
    var type = Reminder.BY_MONTH
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
      type = if (binding.actionView.actionState == ActionView.ActionState.CALL) {
        Reminder.BY_MONTH_CALL
      } else {
        Reminder.BY_MONTH_SMS
      }
    }
    reminder.weekdays = listOf()
    reminder.target = number
    reminder.type = type
    reminder.dayOfMonth = iFace.state.day
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.of(LocalDate.now(), time))
    if (reminder.repeatInterval <= 0) {
      reminder.repeatInterval = 1
    }
    val startTime = dateTimeManager.getNewNextMonthDayTime(reminder)
    if (reminder.remindBefore > 0 &&
      !dateTimeManager.isCurrent(startTime.minusMillis(reminder.remindBefore))
    ) {
      iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
      return null
    }
    Timber.d("EVENT_TIME %s", dateTimeManager.logDateTime(startTime))
    if (!dateTimeManager.isCurrent(startTime)) {
      iFace.showSnackbar(getString(R.string.reminder_is_outdated))
      return null
    }
    reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(startTime)
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
  ) = FragmentReminderMonthBinding.inflate(inflater, container, false)

  override fun getDynamicViews(): List<View> {
    return listOfNotNull(
      binding.ledView,
      binding.exportToCalendar,
      binding.exportToTasks,
      binding.tuneExtraView,
      binding.melodyView,
      binding.attachmentView,
      binding.groupView,
      binding.taskSummary,
      binding.beforeView,
      binding.loudnessView,
      binding.priorityView,
      binding.repeatLimitView,
      binding.repeatView,
      binding.windowTypeView,
      binding.actionView
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.monthDayField.setOnClickListener {
      dateTimePickerProvider.showDatePicker(requireContext(), getDate()) {
        onDateSelected(it)
      }
    }
    binding.timeField.setOnClickListener {
      dateTimePickerProvider.showTimePicker(requireContext(), time) { onTimeSelected(it) }
    }
    binding.timeField.text = dateTimeManager.getTime(time)
    binding.repeatView.defaultValue = 1

    binding.tuneExtraView.hasAutoExtra = false
    binding.lastCheck.setOnCheckedChangeListener { _, b ->
      iFace.state.isLastDay = b
      changeUi(b)
    }

    if (!iFace.state.isLastDay) {
      binding.dayCheck.isChecked = true
    }
    changeUi(iFace.state.isLastDay)

    showSelectedDay()
    editReminder()
    calculateNextDate()
  }

  private fun getDate(): LocalDate  {
    return LocalDate.of(iFace.state.year, iFace.state.month + 1, iFace.state.day)
  }

  private fun onDateSelected(date: LocalDate) {
    iFace.state.day = date.dayOfMonth
    iFace.state.month = date.monthValue - 1
    iFace.state.year = date.year
    showSelectedDay()
    calculateNextDate()
  }

  private fun onTimeSelected(time: LocalTime) {
    iFace.state.hour = time.hour
    iFace.state.minute = time.minute
    binding.timeField.text = dateTimeManager.getTime(time)
    calculateNextDate()
  }

  private fun calculateNextDate() {
    val reminder = Reminder()
    reminder.type = Reminder.BY_MONTH
    reminder.dayOfMonth = iFace.state.day
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.of(LocalDate.now(), time))
    if (reminder.repeatInterval <= 0) {
      reminder.repeatInterval = 1
    }
    Timber.d("calculateNextDate: $reminder")
    val startTime = dateTimeManager.getNewNextMonthDayTime(reminder)
    binding.calculatedNextTime.text = dateTimeManager.getFullDateTime(startTime)
  }

  override fun updateActions() {
    if (binding.actionView.hasAction()) {
      if (binding.actionView.actionState == ActionView.ActionState.SMS) {
        binding.tuneExtraView.hasAutoExtra = false
      } else {
        binding.tuneExtraView.hasAutoExtra = true
        binding.tuneExtraView.hint = getString(R.string.enable_making_phone_calls_automatically)
      }
    } else {
      binding.tuneExtraView.hasAutoExtra = false
    }
  }

  private fun showSelectedDay() {
    if (iFace.state.day <= 0) {
      iFace.state.day = LocalDate.now().dayOfMonth
    }
    Timber.d("showSelectedDay: ${iFace.state.day}")
    binding.monthDayField.text = getZeroedInt(iFace.state.day)
  }

  private fun changeUi(b: Boolean) {
    if (b) {
      binding.dayView.visibility = View.GONE
      iFace.state.day = 0
    } else {
      binding.dayView.visibility = View.VISIBLE
      showSelectedDay()
    }
    calculateNextDate()
  }

  private fun updateTime(time: LocalTime?): LocalTime {
    val localTime = time ?: LocalTime.now()
    iFace.state.hour = localTime.hour
    iFace.state.minute = localTime.minute
    return localTime
  }

  private fun editReminder() {
    val reminder = iFace.state.reminder
    binding.timeField.text = dateTimeManager.getTime(
      updateTime(dateTimeManager.fromGmtToLocal(reminder.eventTime)?.toLocalTime())
    )
    if (iFace.state.isLastDay && reminder.dayOfMonth == 0) {
      binding.lastCheck.isChecked = true
    } else {
      iFace.state.day = reminder.dayOfMonth
      binding.dayCheck.isChecked = true
      showSelectedDay()
    }
    calculateNextDate()
  }
}
