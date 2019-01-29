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
import kotlinx.android.synthetic.main.fragment_reminder_weekdays.*
import timber.log.Timber
import java.util.*

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
class WeekFragment : RepeatableTypeFragment() {

    private var mHour = 0
    private var mMinute = 0

    private val mTimeSelect = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        mHour = hourOfDay
        mMinute = minute
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        val formattedTime = TimeUtil.getTime(c.time, prefs.is24HourFormat, prefs.appLanguage)
        timeField.text = formattedTime
    }

    private val time: Long
        get() {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, mHour)
            calendar.set(Calendar.MINUTE, mMinute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

    val days: List<Int>
        get() = IntervalUtil.getWeekRepeat(mondayCheck.isChecked,
                tuesdayCheck.isChecked, wednesdayCheck.isChecked,
                thursdayCheck.isChecked, fridayCheck.isChecked,
                saturdayCheck.isChecked, sundayCheck.isChecked)

    private val mCheckListener: CompoundButton.OnCheckedChangeListener =  CompoundButton.OnCheckedChangeListener { _, _ ->
        calculateNextDate()
    }

    override fun prepare(): Reminder? {
        val reminder = reminderInterface.state.reminder
        var type = Reminder.BY_WEEK
        val isAction = actionView.hasAction()
        if (TextUtils.isEmpty(reminder.summary) && !isAction) {
            taskLayout.error = getString(R.string.task_summary_is_empty)
            taskLayout.isErrorEnabled = true
            return null
        }
        var number = ""
        if (isAction) {
            number = actionView.number
            if (TextUtils.isEmpty(number)) {
                reminderInterface.showSnackbar(getString(R.string.you_dont_insert_number))
                return null
            }
            type = if (actionView.type == ActionView.TYPE_CALL) {
                Reminder.BY_WEEK_CALL
            } else {
                Reminder.BY_WEEK_SMS
            }
        }
        val weekdays = days
        if (!IntervalUtil.isWeekday(weekdays)) {
            reminderInterface.showSnackbar(getString(R.string.you_dont_select_any_day))
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
            reminderInterface.showSnackbar(getString(R.string.reminder_is_outdated))
            return null
        }
        return reminder
    }

    override fun layoutRes(): Int = R.layout.fragment_reminder_weekdays

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
                loudnessPickerView = loudnessView,
                priorityPickerView = priorityView,
                repeatLimitView = repeatLimitView,
                windowTypeView = windowTypeView,
                actionView = actionView
        )
    }

    override fun onNewHeader(newHeader: String) {
        cardSummary?.text = newHeader
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sundayCheck.setOnCheckedChangeListener(mCheckListener)
        saturdayCheck.setOnCheckedChangeListener(mCheckListener)
        fridayCheck.setOnCheckedChangeListener(mCheckListener)
        thursdayCheck.setOnCheckedChangeListener(mCheckListener)
        wednesdayCheck.setOnCheckedChangeListener(mCheckListener)
        tuesdayCheck.setOnCheckedChangeListener(mCheckListener)
        mondayCheck.setOnCheckedChangeListener(mCheckListener)

        timeField.setOnClickListener {
            TimeUtil.showTimePicker(activity!!, themeUtil.dialogStyle,
                    prefs.is24HourFormat, mHour, mMinute, mTimeSelect)
        }
        timeField.text = TimeUtil.getTime(updateTime(System.currentTimeMillis()),
                prefs.is24HourFormat, prefs.appLanguage)

        tuneExtraView.hasAutoExtra = false
        calculateNextDate()
        editReminder()
    }

    private fun calculateNextDate() {
        val weekdays = days
        if (!IntervalUtil.isWeekday(weekdays)) {
            calculatedNextTime.text = ""
            return
        }
        val reminder = Reminder()
        reminder.weekdays = weekdays
        reminder.type = Reminder.BY_WEEK
        reminder.repeatInterval = 0
        reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
        val startTime = TimeCount.getNextWeekdayTime(reminder)
        calculatedNextTime.text = TimeUtil.getFullDateTime(startTime, prefs.is24HourFormat, prefs.appLanguage)
    }

    override fun updateActions() {
        if (actionView.hasAction()) {
            tuneExtraView.hasAutoExtra = true
            if (actionView.type == ActionView.TYPE_MESSAGE) {
                tuneExtraView.hint = getString(R.string.enable_sending_sms_automatically)
            } else {
                tuneExtraView.hint = getString(R.string.enable_making_phone_calls_automatically)
            }
        } else {
            tuneExtraView.hasAutoExtra = false
        }
    }

    private fun updateTime(millis: Long): Date {
        val cal = Calendar.getInstance()
        cal.timeInMillis = if (millis != 0L) millis else System.currentTimeMillis()
        mHour = cal.get(Calendar.HOUR_OF_DAY)
        mMinute = cal.get(Calendar.MINUTE)
        return cal.time
    }

    private fun setCheckForDays(weekdays: List<Int>) {
        sundayCheck.isChecked = weekdays[0] == 1
        mondayCheck.isChecked = weekdays[1] == 1
        tuesdayCheck.isChecked = weekdays[2] == 1
        wednesdayCheck.isChecked = weekdays[3] == 1
        thursdayCheck.isChecked = weekdays[4] == 1
        fridayCheck.isChecked = weekdays[5] == 1
        saturdayCheck.isChecked = weekdays[6] == 1
    }

    private fun editReminder() {
        val reminder = reminderInterface.state.reminder
        showGroup(groupView, reminder)
        timeField.text = TimeUtil.getTime(updateTime(TimeUtil.getDateTimeFromGmt(reminder.eventTime)),
                prefs.is24HourFormat, prefs.appLanguage)
        if (reminder.weekdays.isNotEmpty()) {
            setCheckForDays(reminder.weekdays)
        }
        calculateNextDate()
        if (reminder.target != "") {
            actionView.setAction(true)
            actionView.number = reminder.target
            if (Reminder.isKind(reminder.type, Reminder.Kind.CALL)) {
                actionView.type = ActionView.TYPE_CALL
            } else if (Reminder.isKind(reminder.type, Reminder.Kind.SMS)) {
                actionView.type = ActionView.TYPE_MESSAGE
            }
        }
    }
}
