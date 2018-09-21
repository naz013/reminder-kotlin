package com.elementary.tasks.reminder.createEdit.fragments

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.views.ActionView
import kotlinx.android.synthetic.main.fragment_weekdays.*
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
        val formattedTime = TimeUtil.getTime(c.time, prefs.is24HourFormatEnabled)
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

    override fun prepare(): Reminder? {
        val iFace = reminderInterface ?: return null
        var type = Reminder.BY_WEEK
        val isAction = actionView.hasAction()
//        if (TextUtils.isEmpty(iFace.summary) && !isAction) {
//            iFace.showSnackbar(getString(R.string.task_summary_is_empty))
//            return null
//        }
        var number = ""
        if (isAction) {
            number = actionView.number
            if (TextUtils.isEmpty(number)) {
                iFace.showSnackbar(getString(R.string.you_dont_insert_number))
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
            Toast.makeText(context, getString(R.string.you_dont_select_any_day), Toast.LENGTH_SHORT).show()
            return null
        }
        var reminder = iFace.reminder
        if (reminder == null) {
            reminder = Reminder()
        }
        reminder.weekdays = weekdays
        reminder.target = number
        reminder.type = type
        reminder.repeatInterval = 0
        reminder.exportToCalendar = exportToCalendar.isChecked
        reminder.exportToTasks = exportToTasks.isChecked
        reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
        reminder.remindBefore = before_view.beforeValue
        val startTime = timeCount.getNextWeekdayTime(reminder)
        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
        LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            Toast.makeText(context, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show()
            return null
        }
        return reminder
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_date_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
//            R.id.action_limit -> changeLimit()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_weekdays, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timeField.setOnClickListener { TimeUtil.showTimePicker(activity!!, prefs.is24HourFormatEnabled, mTimeSelect, mHour, mMinute) }
        timeField.text = TimeUtil.getTime(updateTime(System.currentTimeMillis()),
                prefs.is24HourFormatEnabled)
        actionView.setActivity(activity!!)
        actionView.setContactClickListener(View.OnClickListener { selectContact() })
        setToggleTheme()
        initScreenState()
        editReminder()
    }

    private fun initScreenState() {
        if (reminderInterface.canExportToCalendar) {
            exportToCalendar.visibility = View.VISIBLE
        } else {
            exportToCalendar.visibility = View.GONE
        }
        if (reminderInterface.canExportToTasks) {
            exportToTasks.visibility = View.VISIBLE
        } else {
            exportToTasks.visibility = View.GONE
        }
    }

    @Suppress("DEPRECATION")
    private fun setToggleTheme() {
        mondayCheck.setBackgroundDrawable(themeUtil.toggleDrawable())
        tuesdayCheck.setBackgroundDrawable(themeUtil.toggleDrawable())
        wednesdayCheck.setBackgroundDrawable(themeUtil.toggleDrawable())
        thursdayCheck.setBackgroundDrawable(themeUtil.toggleDrawable())
        fridayCheck.setBackgroundDrawable(themeUtil.toggleDrawable())
        saturdayCheck.setBackgroundDrawable(themeUtil.toggleDrawable())
        sundayCheck.setBackgroundDrawable(themeUtil.toggleDrawable())
    }

    private fun updateTime(millis: Long): Date {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
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
        val iFace = reminderInterface ?: return
        val reminder = iFace.reminder ?: return
        exportToCalendar.isChecked = reminder.exportToCalendar
        exportToTasks.isChecked = reminder.exportToTasks
        timeField.text = TimeUtil.getTime(updateTime(TimeUtil.getDateTimeFromGmt(reminder.eventTime)),
                prefs.is24HourFormatEnabled)
        before_view.setBefore(reminder.remindBefore)
        if (reminder.weekdays.isNotEmpty()) {
            setCheckForDays(reminder.weekdays)
        }
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

    private fun selectContact() {
        if (Permissions.checkPermission(activity!!, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            SuperUtil.selectContact(activity!!, Constants.REQUEST_CODE_CONTACTS)
        } else {
            Permissions.requestPermission(activity!!, CONTACTS, Permissions.READ_CONTACTS, Permissions.READ_CALLS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS && resultCode == Activity.RESULT_OK) {
            val number = data?.getStringExtra(Constants.SELECTED_CONTACT_NUMBER) ?: ""
            actionView.number = number
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        actionView.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            CONTACTS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectContact()
            }
        }
    }

    companion object {

        private const val TAG = "WeekFragment"
        private const val CONTACTS = 112
    }
}
