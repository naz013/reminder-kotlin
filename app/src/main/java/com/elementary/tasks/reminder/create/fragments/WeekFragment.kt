package com.elementary.tasks.reminder.create.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.IntervalUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.databinding.FragmentReminderWeekdaysBinding
import timber.log.Timber
import java.util.*

class WeekFragment : RepeatableTypeFragment<FragmentReminderWeekdaysBinding>() {

    private val mTimeSelect = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        iFace.state.hour = hourOfDay
        iFace.state.minute = minute
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        val formattedTime = TimeUtil.getTime(c.time, prefs.is24HourFormat, prefs.appLanguage)
        binding.timeField.text = formattedTime
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

    val days: List<Int>
        get() = IntervalUtil.getWeekRepeat(binding.mondayCheck.isChecked,
                binding.tuesdayCheck.isChecked, binding.wednesdayCheck.isChecked,
                binding.thursdayCheck.isChecked, binding.fridayCheck.isChecked,
                binding.saturdayCheck.isChecked, binding.sundayCheck.isChecked)

    private val mCheckListener: CompoundButton.OnCheckedChangeListener =  CompoundButton.OnCheckedChangeListener { _, _ ->
        iFace.state.weekdays = days
        iFace.state.isWeekdaysSaved = true
        calculateNextDate()
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
            type = if (binding.actionView.type == ActionView.TYPE_CALL) {
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
        reminder.repeatInterval = 0
        reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
        val startTime = TimeCount.getNextWeekdayTime(reminder)
        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
        Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            iFace.showSnackbar(getString(R.string.reminder_is_outdated))
            return null
        }
        return reminder
    }

    override fun layoutRes(): Int = R.layout.fragment_reminder_weekdays

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
                windowTypeView = binding.windowTypeView,
                actionView = binding.actionView
        )
    }

    override fun onNewHeader(newHeader: String) {
        binding.cardSummary.text = newHeader
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
            TimeUtil.showTimePicker(activity!!, themeUtil.dialogStyle,
                    prefs.is24HourFormat, iFace.state.hour, iFace.state.minute, mTimeSelect)
        }
        binding.timeField.text = TimeUtil.getTime(time, prefs.is24HourFormat, prefs.appLanguage)

        binding.tuneExtraView.hasAutoExtra = false
        calculateNextDate()
        editReminder()
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
        reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
        val startTime = TimeCount.getNextWeekdayTime(reminder)
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

    private fun updateTime(millis: Long): Date {
        val cal = Calendar.getInstance()
        cal.timeInMillis = if (millis != 0L) millis else System.currentTimeMillis()
        iFace.state.hour = cal.get(Calendar.HOUR_OF_DAY)
        iFace.state.minute = cal.get(Calendar.MINUTE)
        return cal.time
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
        binding.timeField.text = TimeUtil.getTime(updateTime(TimeUtil.getDateTimeFromGmt(reminder.eventTime)),
                prefs.is24HourFormat, prefs.appLanguage)
        if (reminder.weekdays.isNotEmpty()) {
            setCheckForDays(reminder.weekdays)
        }
        if (iFace.state.isWeekdaysSaved) {
            setCheckForDays(iFace.state.weekdays)
        }
        calculateNextDate()
    }
}
