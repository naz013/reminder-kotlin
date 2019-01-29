package com.elementary.tasks.reminder.create.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.views.ActionView
import kotlinx.android.synthetic.main.fragment_reminder_month.*
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
class MonthFragment : RepeatableTypeFragment() {

    private val mTimeSelect = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        iFace.state.hour = hourOfDay
        iFace.state.minute = minute
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        val formattedTime = TimeUtil.getTime(c.time, prefs.is24HourFormat, prefs.appLanguage)
        timeField.text = formattedTime
        calculateNextDate()
    }
    private val mDateSelect = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        if (dayOfMonth > 28) {
            iFace.showSnackbar(getString(R.string.max_day_supported))
            return@OnDateSetListener
        }
        iFace.state.day = dayOfMonth
        iFace.state.month = monthOfYear
        iFace.state.year = year
        monthDayField.text = getZeroedInt(iFace.state.day)
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
                iFace.showSnackbar(getString(R.string.you_dont_insert_number))
                return null
            }
            type = if (actionView.type == ActionView.TYPE_CALL) {
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
        return reminder
    }

    override fun layoutRes(): Int = R.layout.fragment_reminder_month

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
                repeatView = repeatView,
                windowTypeView = windowTypeView,
                actionView = actionView
        )
    }

    override fun onNewHeader(newHeader: String) {
        cardSummary?.text = newHeader
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        monthDayField.setOnClickListener {
            TimeUtil.showDatePicker(activity!!, themeUtil.dialogStyle, prefs, iFace.state.year,
                    iFace.state.month, iFace.state.day, mDateSelect)
        }
        timeField.setOnClickListener {
            TimeUtil.showTimePicker(activity!!, themeUtil.dialogStyle, prefs.is24HourFormat,
                    iFace.state.hour, iFace.state.minute, mTimeSelect)
        }
        timeField.text = TimeUtil.getTime(time, prefs.is24HourFormat, prefs.appLanguage)

        tuneExtraView.hasAutoExtra = false
        lastCheck.setOnCheckedChangeListener { _, b ->
            iFace.state.isLastDay = b
            changeUi(b)
        }
        if (!iFace.state.isLastDay) dayCheck.isChecked = true

        calculateNextDate()
        editReminder()
        showSelectedDay()
    }

    private fun calculateNextDate() {
        val reminder = Reminder()
        reminder.weekdays = listOf()
        reminder.type = Reminder.BY_MONTH
        reminder.dayOfMonth = iFace.state.day
        reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
        if (reminder.repeatInterval <= 0) {
            reminder.repeatInterval = 1
        }
        val startTime = TimeCount.getNextMonthDayTime(reminder)
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

    private fun showSelectedDay() {
        if (iFace.state.day == 0) {
            iFace.state.day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        }
        if (iFace.state.day > 28) iFace.state.day = 28
        monthDayField.text = getZeroedInt(iFace.state.day)
    }

    private fun changeUi(b: Boolean) {
        if (b) {
            day_view.visibility = View.GONE
            iFace.state.day = 0
        } else {
            day_view.visibility = View.VISIBLE
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            iFace.state.day = calendar.get(Calendar.DAY_OF_MONTH)
            iFace.state.month = calendar.get(Calendar.MONTH)
            iFace.state.year = calendar.get(Calendar.YEAR)
            if (iFace.state.day > 28) iFace.state.day = 1
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
        timeField.text = TimeUtil.getTime(updateTime(TimeUtil.getDateTimeFromGmt(reminder.eventTime)),
                prefs.is24HourFormat, prefs.appLanguage)
        if (iFace.state.isLastDay && reminder.dayOfMonth == 0) {
            lastCheck.isChecked = true
        } else {
            iFace.state.day = reminder.dayOfMonth
            dayCheck.isChecked = true
        }
        calculateNextDate()
    }
}
