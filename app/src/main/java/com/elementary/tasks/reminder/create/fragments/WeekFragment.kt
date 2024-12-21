package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.elementary.tasks.R
import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.utils.datetime.IntervalUtil
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.ClosableLegacyBuilderWarningView
import com.elementary.tasks.databinding.FragmentReminderWeekdaysBinding
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class WeekFragment : RepeatableTypeFragment<FragmentReminderWeekdaysBinding>() {

  private val time: LocalTime
    get() = LocalTime.of(iFace.state.hour, iFace.state.minute)

  val days: List<Int>
    get() = IntervalUtil.getWeekRepeat(
      binding.mondayCheck.isChecked,
      binding.tuesdayCheck.isChecked,
      binding.wednesdayCheck.isChecked,
      binding.thursdayCheck.isChecked,
      binding.fridayCheck.isChecked,
      binding.saturdayCheck.isChecked,
      binding.sundayCheck.isChecked
    )

  private val mCheckListener: CompoundButton.OnCheckedChangeListener =
    CompoundButton.OnCheckedChangeListener { _, _ ->
      iFace.state.weekdays = days
      iFace.state.isWeekdaysSaved = true
      calculateNextDate()
    }

  override fun getExplanationVisibilityType(): ReminderExplanationVisibility.Type {
    return ReminderExplanationVisibility.Type.BY_WEEKDAY
  }

  override fun getExplanationView(): View {
    return binding.explanationView
  }

  override fun setCloseListenerToExplanationView(listener: View.OnClickListener) {
    binding.explanationView.setOnClickListener(listener)
  }

  override fun prepare(): Reminder? {
    val reminder = iFace.state.reminder
    var type = Reminder.BY_WEEK
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
        Reminder.BY_WEEK_CALL
      } else {
        Reminder.BY_WEEK_SMS
      }
    }
    val weekdays = iFace.state.weekdays
    if (!IntervalUtil.isWeekday(weekdays)) {
      iFace.showSnackbar(getString(R.string.you_dont_select_any_day))
      return null
    }

    reminder.weekdays = weekdays
    reminder.target = number
    reminder.type = type
    reminder.after = 0L
    reminder.delay = 0
    reminder.eventCount = 0
    reminder.repeatInterval = 0
    reminder.recurDataObject = null

    reminder.eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.of(LocalDate.now(), time))
    val startTime = dateTimeManager.getNextWeekdayTime(reminder)
    if (!dateTimeManager.isCurrent(startTime)) {
      iFace.showSnackbar(getString(R.string.reminder_is_outdated))
      return null
    }
    reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(startTime)
    Logger.d("EVENT_TIME ${dateTimeManager.logDateTime(startTime)}")
    return reminder
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderWeekdaysBinding.inflate(inflater, container, false)

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
      binding.priorityView,
      binding.repeatLimitView,
      binding.actionView
    )
  }

  override fun getLegacyMessageView(): ClosableLegacyBuilderWarningView {
    return binding.legacyBuilderWarningView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.sundayCheck.setOnCheckedChangeListener(mCheckListener)
    binding.saturdayCheck.setOnCheckedChangeListener(mCheckListener)
    binding.fridayCheck.setOnCheckedChangeListener(mCheckListener)
    binding.thursdayCheck.setOnCheckedChangeListener(mCheckListener)
    binding.wednesdayCheck.setOnCheckedChangeListener(mCheckListener)
    binding.tuesdayCheck.setOnCheckedChangeListener(mCheckListener)
    binding.mondayCheck.setOnCheckedChangeListener(mCheckListener)

    binding.timeField.setOnClickListener {
      dateTimePickerProvider.showTimePicker(
        fragmentManager = childFragmentManager,
        time = time,
        title = getString(R.string.select_time)
      ) {
        onTimeSelected(it)
      }
    }
    binding.timeField.text = dateTimeManager.getTime(time)

    binding.tuneExtraView.hasAutoExtra = false
    calculateNextDate()
    editReminder()
  }

  private fun onTimeSelected(localTime: LocalTime) {
    iFace.state.hour = localTime.hour
    iFace.state.minute = localTime.minute
    binding.timeField.text = dateTimeManager.getTime(localTime)
    calculateNextDate()
  }

  private fun calculateNextDate() {
    val weekdays = days
    if (!IntervalUtil.isWeekday(weekdays)) {
      binding.calculatedNextTime.text = ""
      return
    }
    val reminder = Reminder()
    reminder.weekdays = weekdays
    reminder.type = Reminder.BY_WEEK
    reminder.repeatInterval = 0
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.of(LocalDate.now(), time))
    val startTime = dateTimeManager.getNextWeekdayTime(reminder)
    binding.calculatedNextTime.text = dateTimeManager.getFullDateTime(startTime)
  }

  override fun updateActions() {
    if (!isAdded) return
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

  private fun updateTime(time: LocalTime?): LocalTime {
    val localTime = time ?: LocalTime.now()
    iFace.state.hour = localTime.hour
    iFace.state.minute = localTime.minute
    return localTime
  }

  private fun setCheckForDays(weekdays: List<Int>) {
    binding.sundayCheck.isChecked = weekdays[0] == 1
    binding.mondayCheck.isChecked = weekdays[1] == 1
    binding.tuesdayCheck.isChecked = weekdays[2] == 1
    binding.wednesdayCheck.isChecked = weekdays[3] == 1
    binding.thursdayCheck.isChecked = weekdays[4] == 1
    binding.fridayCheck.isChecked = weekdays[5] == 1
    binding.saturdayCheck.isChecked = weekdays[6] == 1
  }

  private fun editReminder() {
    val reminder = iFace.state.reminder
    binding.timeField.text = dateTimeManager.getTime(
      updateTime(dateTimeManager.fromGmtToLocal(reminder.eventTime)?.toLocalTime())
    )
    if (reminder.weekdays.isNotEmpty()) {
      setCheckForDays(reminder.weekdays)
    }
    if (iFace.state.isWeekdaysSaved) {
      setCheckForDays(iFace.state.weekdays)
    }
    calculateNextDate()
  }
}
