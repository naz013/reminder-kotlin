package com.elementary.tasks.reminder.createEdit.fragments

import android.app.Activity
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
import com.elementary.tasks.core.views.DateTimeView
import kotlinx.android.synthetic.main.fragment_reminder_year.*
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
class YearFragment : RepeatableTypeFragment() {

    private var mHour = 0
    private var mMinute = 0
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 1

    private val mActionListener = object : ActionView.OnActionListener {
        override fun onActionChange(hasAction: Boolean) {
            if (!hasAction) {
                reminderInterface?.setEventHint(getString(R.string.remind_me))
                reminderInterface?.setHasAutoExtra(false, "")
            }
        }

        override fun onTypeChange(isMessageType: Boolean) {
            if (isMessageType) {
                reminderInterface?.setEventHint(getString(R.string.message))
                reminderInterface?.setHasAutoExtra(true, getString(R.string.enable_sending_sms_automatically))
            } else {
                reminderInterface?.setEventHint(getString(R.string.remind_me))
                reminderInterface?.setHasAutoExtra(true, getString(R.string.enable_making_phone_calls_automatically))
            }
        }
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

    override fun prepare(): Reminder? {
        val iFace = reminderInterface ?: return null
        var type = Reminder.BY_DAY_OF_YEAR
        val isAction = actionView.hasAction()
        if (TextUtils.isEmpty(iFace.summary) && !isAction) {
            iFace.showSnackbar(getString(R.string.task_summary_is_empty))
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
                Reminder.BY_DAY_OF_YEAR_CALL
            } else {
                Reminder.BY_DAY_OF_YEAR_SMS
            }
        }
        var reminder = iFace.reminder
        if (reminder == null) {
            reminder = Reminder()
        }
        reminder.weekdays = listOf()
        reminder.target = number
        reminder.type = type
        reminder.dayOfMonth = mDay
        reminder.monthOfYear = mMonth
        reminder.repeatInterval = 0
        reminder.exportToCalendar = exportToCalendar.isChecked
        reminder.exportToTasks = exportToTasks.isChecked
        reminder.setClear(iFace)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
        reminder.remindBefore = before_view.beforeValue
        val startTime = timeCount.getNextYearDayTime(reminder)
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
            R.id.action_limit -> changeLimit()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminder_year, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dateView.setDateFormat(TimeUtil.SIMPLE_DATE)
        dateView.setEventListener(object : DateTimeView.OnSelectListener {
            override fun onDateSelect(mills: Long, day: Int, month: Int, year: Int) {
                if (month == 1 && day > 28) {
                    reminderInterface!!.showSnackbar(getString(R.string.max_day_supported))
                    return
                }
                mDay = day
                mMonth = month
                mYear = year
            }

            override fun onTimeSelect(mills: Long, hour: Int, minute: Int) {
                mHour = hour
                mMinute = minute
            }
        })
        actionView.setListener(mActionListener)
        actionView.setActivity(activity!!)
        actionView.setContactClickListener(View.OnClickListener { selectContact() })
        initScreenState()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        mDay = calendar.get(Calendar.DAY_OF_MONTH)
        mMonth = calendar.get(Calendar.MONTH)
        mYear = calendar.get(Calendar.YEAR)
        mHour = calendar.get(Calendar.HOUR_OF_DAY)
        mMinute = calendar.get(Calendar.MINUTE)
        dateView.dateTime = System.currentTimeMillis()
        editReminder()
    }

    private fun initScreenState() {
        val iFace = reminderInterface ?: return
        if (iFace.isExportToCalendar) {
            exportToCalendar.visibility = View.VISIBLE
        } else {
            exportToCalendar.visibility = View.GONE
        }
        if (iFace.isExportToTasks) {
            exportToTasks.visibility = View.VISIBLE
        } else {
            exportToTasks.visibility = View.GONE
        }
    }

    private fun updateDateTime(reminder: Reminder) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        mHour = calendar.get(Calendar.HOUR_OF_DAY)
        mMinute = calendar.get(Calendar.MINUTE)
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.DAY_OF_MONTH, reminder.dayOfMonth)
        calendar.set(Calendar.MONTH, reminder.monthOfYear)
        calendar.set(Calendar.HOUR_OF_DAY, mHour)
        calendar.set(Calendar.MINUTE, mMinute)
        dateView.dateTime = calendar.timeInMillis
        mDay = reminder.dayOfMonth
        mMonth = reminder.monthOfYear
    }

    private fun editReminder() {
        val iFace = reminderInterface ?: return
        val reminder = iFace.reminder ?: return
        exportToCalendar.isChecked = reminder.exportToCalendar
        exportToTasks.isChecked = reminder.exportToTasks
        before_view.setBefore(reminder.remindBefore)
        updateDateTime(reminder)
        mDay = reminder.dayOfMonth
        mMonth = reminder.monthOfYear
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
        private const val CONTACTS = 114
    }
}
