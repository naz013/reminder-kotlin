package com.elementary.tasks.reminder.create.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.toCalendar
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.databinding.FragmentReminderMonthBinding
import timber.log.Timber
import java.util.*

class MonthFragment : RepeatableTypeFragment<FragmentReminderMonthBinding>() {

  private val mTimeSelect = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
    iFace.state.hour = hourOfDay
    iFace.state.minute = minute
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, hourOfDay)
    c.set(Calendar.MINUTE, minute)
    binding.timeField.text = TimeUtil.getTime(c.time, prefs.is24HourFormat, prefs.appLanguage)
    calculateNextDate()
  }
  private val mDateSelect = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
    iFace.state.day = dayOfMonth
    iFace.state.month = monthOfYear
    iFace.state.year = year
    showSelectedDay()
    calculateNextDate()
  }

  private val time: Long
    get() {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = System.currentTimeMillis()
      calendar.set(Calendar.HOUR_OF_DAY, iFace.state.hour)
      calendar.set(Calendar.MINUTE, iFace.state.minute)
      calendar.set(Calendar.SECOND, 0)
      calendar.set(Calendar.MILLISECOND, 0)
      return calendar.timeInMillis
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
      type = if (binding.actionView.type == ActionView.TYPE_CALL) {
        Reminder.BY_MONTH_CALL
      } else {
        Reminder.BY_MONTH_SMS
      }
    }
    reminder.weekdays = listOf()
    reminder.target = number
    reminder.type = type
    reminder.dayOfMonth = iFace.state.day
    reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
    if (reminder.repeatInterval <= 0) {
      reminder.repeatInterval = 1
    }
    val startTime = TimeCount.getNextMonthDayTime(reminder)
    reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
    reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
    if (reminder.remindBefore > 0 && startTime - reminder.remindBefore < System.currentTimeMillis()) {
      iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
      return null
    }
    Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
    if (!TimeCount.isCurrent(reminder.eventTime)) {
      iFace.showSnackbar(getString(R.string.reminder_is_outdated))
      return null
    }
    reminder.after = 0L
    reminder.delay = 0
    reminder.eventCount = 0
    return reminder
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderMonthBinding.inflate(inflater, container, false)

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
    binding.monthDayField.setOnClickListener {
      TimeUtil.showDatePicker(requireContext(), prefs, iFace.state.year, iFace.state.month,
        iFace.state.day, mDateSelect)
    }
    binding.timeField.setOnClickListener {
      TimeUtil.showTimePicker(requireContext(), prefs.is24HourFormat, iFace.state.hour,
        iFace.state.minute, mTimeSelect)
    }
    binding.timeField.text = TimeUtil.getTime(time, prefs.is24HourFormat, prefs.appLanguage)
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

  private fun calculateNextDate() {
    val reminder = Reminder()
    reminder.type = Reminder.BY_MONTH
    reminder.dayOfMonth = iFace.state.day
    reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
    if (reminder.repeatInterval <= 0) {
      reminder.repeatInterval = 1
    }
    Timber.d("calculateNextDate: $reminder")
    val startTime = TimeCount.getNextMonthDayTime(reminder)
    binding.calculatedNextTime.text = TimeUtil.getFullDateTime(startTime, prefs.is24HourFormat, prefs.appLanguage)
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

  private fun showSelectedDay() {
    if (iFace.state.day <= 0) {
      iFace.state.day = System.currentTimeMillis().toCalendar().get(Calendar.DAY_OF_MONTH)
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

  private fun updateTime(millis: Long): Date {
    val cal = Calendar.getInstance()
    cal.timeInMillis = if (millis != 0L) millis else System.currentTimeMillis()
    iFace.state.hour = cal.get(Calendar.HOUR_OF_DAY)
    iFace.state.minute = cal.get(Calendar.MINUTE)
    return cal.time
  }

  private fun editReminder() {
    val reminder = iFace.state.reminder
    binding.timeField.text = TimeUtil.getTime(updateTime(TimeUtil.getDateTimeFromGmt(reminder.eventTime)),
      prefs.is24HourFormat, prefs.appLanguage)
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
