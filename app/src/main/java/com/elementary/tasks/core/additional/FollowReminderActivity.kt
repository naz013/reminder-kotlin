package com.elementary.tasks.core.additional

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.SpinnerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import kotlinx.android.synthetic.main.activity_follow.*
import java.util.*
import javax.inject.Inject

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
open class FollowReminderActivity : ThemedActivity(), CompoundButton.OnCheckedChangeListener {

    private var viewModel: ReminderViewModel? = null

    private var mHour = 0
    private var mCustomHour = 0
    private var mMinute = 0
    private var mCustomMinute = 0
    private var mYear = 0
    private var mCustomYear = 0
    private var mMonth = 0
    private var mCustomMonth = 0
    private var mDay = 1
    private var mCustomDay = 1
    private var mTomorrowTime: Long = 0
    private var mNextWorkTime: Long = 0
    private var mCurrentTime: Long = 0

    private var mIs24Hour = true
    private var mCalendar = true
    private var mStock = true
    private var mTasks = true
    private var mNumber: String = ""
    private var mGoogleTasks: Google? = null
    @Inject lateinit var reminderUtils: ReminderUtils

    private val adapter: SpinnerAdapter
        get() {
            val spinnerArray = ArrayList<String>()
            spinnerArray.add(String.format(getString(R.string.x_minutes), 5.toString()))
            spinnerArray.add(String.format(getString(R.string.x_minutes), 10.toString()))
            spinnerArray.add(String.format(getString(R.string.x_minutes), 15.toString()))
            spinnerArray.add(String.format(getString(R.string.x_minutes), 30.toString()))
            spinnerArray.add(String.format(getString(R.string.x_minutes), 45.toString()))
            spinnerArray.add(String.format(getString(R.string.x_minutes), 60.toString()))
            spinnerArray.add(String.format(getString(R.string.x_hours), 2.toString()))
            spinnerArray.add(String.format(getString(R.string.x_hours), 3.toString()))
            spinnerArray.add(String.format(getString(R.string.x_hours), 4.toString()))
            spinnerArray.add(String.format(getString(R.string.x_hours), 5.toString()))
            return ArrayAdapter(this, android.R.layout.simple_list_item_1, spinnerArray)
        }

    private var myDateCallBack: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        mCustomYear = year
        mCustomMonth = monthOfYear
        mCustomDay = dayOfMonth

        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, monthOfYear)
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        customDate.text = TimeUtil.DATE_FORMAT.format(c.time)
    }

    private var myCallBack: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        mCustomHour = hourOfDay
        mCustomMinute = minute

        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)

        customTime.text = TimeUtil.getTime(c.time, mIs24Hour)
    }

    private val type: Int
        get() = if (typeCall.isChecked)
            Reminder.BY_DATE_CALL
        else
            Reminder.BY_DATE_SMS

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGoogleTasks = Google.getInstance()
        val i = intent
        val receivedDate = i.getLongExtra(Constants.SELECTED_TIME, 0)
        mNumber = i.getStringExtra(Constants.SELECTED_CONTACT_NUMBER)
        val name = Contacts.getNameFromNumber(mNumber, this@FollowReminderActivity)
        setContentView(R.layout.activity_follow)
        initActionBar()
        val c = Calendar.getInstance()
        if (receivedDate != 0L) {
            c.timeInMillis = receivedDate
        } else
            c.timeInMillis = System.currentTimeMillis()
        mCurrentTime = c.timeInMillis

        textField.hint = getString(R.string.message)

        val contactInfo = contactInfo
        if (name != null && !name.matches("".toRegex())) {
            contactInfo.text = SuperUtil.appendString(name, "\n", mNumber)
        } else {
            contactInfo.text = mNumber
        }
        initViews()
        initPrefs()
        initExportChecks()
        initSpinner()
        initCustomTime()
        initTomorrowTime()
        initNextBusinessTime()

        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, ReminderViewModel.Factory(application, "")).get(ReminderViewModel::class.java)
        viewModel!!.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED -> closeWindow()
                    else -> {
                    }
                }
            }
        })
    }

    private fun initViews() {
        typeCall.isChecked = true
        timeTomorrow.setOnCheckedChangeListener(this)
        timeAfter.setOnCheckedChangeListener(this)
        timeCustom.setOnCheckedChangeListener(this)
        timeNextWorking.setOnCheckedChangeListener(this)
        timeTomorrow.isChecked = true
    }

    private fun initNextBusinessTime() {
        val c = Calendar.getInstance()
        c.timeInMillis = mCurrentTime
        val currDay = c.get(Calendar.DAY_OF_WEEK)
        when (currDay) {
            Calendar.FRIDAY -> c.timeInMillis = mCurrentTime + 1000 * 60 * 60 * 24 * 3
            Calendar.SATURDAY -> c.timeInMillis = mCurrentTime + 1000 * 60 * 60 * 24 * 2
            else -> c.timeInMillis = mCurrentTime + 1000 * 60 * 60 * 24
        }
        mNextWorkTime = c.timeInMillis
        val nextWorkingTime = nextWorkingTime
        nextWorkingTime.text = TimeUtil.getDateTime(c.time, mIs24Hour)
    }

    private fun initTomorrowTime() {
        val c = Calendar.getInstance()
        c.timeInMillis = mCurrentTime + 1000 * 60 * 60 * 24
        mTomorrowTime = c.timeInMillis
        mHour = c.get(Calendar.HOUR_OF_DAY)
        mMinute = c.get(Calendar.MINUTE)
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH)
        mDay = c.get(Calendar.DAY_OF_MONTH)
        tomorrowTime.text = TimeUtil.getDateTime(c.time, mIs24Hour)
    }

    private fun initSpinner() {
        afterTime.adapter = adapter
    }

    private fun initCustomTime() {
        val c = Calendar.getInstance()
        c.timeInMillis = mCurrentTime
        customDate.text = TimeUtil.DATE_FORMAT.format(c.time)
        customTime.text = TimeUtil.getTime(c.time, mIs24Hour)
        mCustomHour = c.get(Calendar.HOUR_OF_DAY)
        mCustomMinute = c.get(Calendar.MINUTE)
        mCustomYear = c.get(Calendar.YEAR)
        mCustomMonth = c.get(Calendar.MONTH)
        mCustomDay = c.get(Calendar.DAY_OF_MONTH)
        customDate.setOnClickListener {
            timeCustom.isChecked = true
            dateDialog()
        }
        customTime.setOnClickListener {
            timeCustom.isChecked = true
            timeDialog()
        }
    }

    private fun initExportChecks() {
        if (mCalendar || mStock) {
            exportCheck.visibility = View.VISIBLE
        }
        if (mTasks) {
            taskExport.visibility = View.VISIBLE
        }
        if (!mCalendar && !mStock && !mTasks) {
            card5.visibility = View.GONE
        }
    }

    private fun initPrefs() {
        mCalendar = prefs.isCalendarEnabled
        mStock = prefs.isStockCalendarEnabled
        mTasks = mGoogleTasks != null
        mIs24Hour = prefs.is24HourFormatEnabled
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        }
        toolbar.setTitle(R.string.create_task)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
    }

    private fun getAfterMins(progress: Int): Int {
        var mins = 0
        when (progress) {
            0 -> mins = 5
            1 -> mins = 10
            2 -> mins = 15
            3 -> mins = 30
            4 -> mins = 45
            5 -> mins = 60
            6 -> mins = 120
            7 -> mins = 180
            8 -> mins = 240
            9 -> mins = 300
        }
        return mins
    }

    private fun dateDialog() {
        TimeUtil.showDatePicker(this, prefs, myDateCallBack, mYear, mMonth, mDay)
    }

    private fun timeDialog() {
        TimeUtil.showTimePicker(this, prefs.is24HourFormatEnabled, myCallBack, mCustomHour, mCustomMinute)
    }

    private fun saveDateTask() {
        val text = textField.text!!.toString().trim { it <= ' ' }
        if (text.matches("".toRegex()) && typeMessage.isChecked) {
            textField.error = getString(R.string.must_be_not_empty)
            return
        }
        val type = type
        setUpTimes()
        val due = reminderUtils.getTime(mDay, mMonth, mYear, mHour, mMinute, 0)
        val reminder = Reminder()
        val def = viewModel!!.defaultReminderGroup.value
        if (def != null) {
            reminder.groupUuId = def.uuId
        }
        reminder.eventTime = TimeUtil.getGmtFromDateTime(due)
        reminder.startTime = TimeUtil.getGmtFromDateTime(due)
        reminder.type = type
        reminder.summary = text
        reminder.target = mNumber
        if (taskExport.visibility == View.VISIBLE) {
            reminder.exportToTasks = taskExport.isChecked
        }
        if (exportCheck.visibility == View.VISIBLE) {
            reminder.exportToCalendar = exportCheck.isChecked
        }
        viewModel!!.saveAndStartReminder(reminder)
    }

    private fun closeWindow() {
        removeFlags()
        finish()
    }

    private fun setUpTimes() {
        when {
            timeNextWorking.isChecked -> setUpNextBusiness()
            timeTomorrow.isChecked -> setUpTomorrow()
            timeCustom.isChecked -> {
                mDay = mCustomDay
                mHour = mCustomHour
                mMinute = mCustomMinute
                mMonth = mCustomMonth
                mYear = mCustomYear
            }
            else -> {
                val c = Calendar.getInstance()
                c.timeInMillis = mCurrentTime + 1000 * 60 * getAfterMins(afterTime.selectedItemPosition)
                mHour = c.get(Calendar.HOUR_OF_DAY)
                mMinute = c.get(Calendar.MINUTE)
                mYear = c.get(Calendar.YEAR)
                mMonth = c.get(Calendar.MONTH)
                mDay = c.get(Calendar.DAY_OF_MONTH)
            }
        }
    }

    private fun removeFlags() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.timeTomorrow -> {
                if (timeTomorrow.isChecked) {
                    timeNextWorking.isChecked = false
                    timeAfter.isChecked = false
                    timeCustom.isChecked = false
                }
                setUpTomorrow()
            }
            R.id.timeNextWorking -> {
                if (timeNextWorking.isChecked) {
                    timeTomorrow.isChecked = false
                    timeAfter.isChecked = false
                    timeCustom.isChecked = false
                }
                setUpNextBusiness()
            }
            R.id.timeAfter -> if (timeAfter.isChecked) {
                timeTomorrow.isChecked = false
                timeNextWorking.isChecked = false
                timeCustom.isChecked = false
            }
            R.id.timeCustom -> if (timeCustom.isChecked) {
                timeTomorrow.isChecked = false
                timeNextWorking.isChecked = false
                timeAfter.isChecked = false
            }
        }
    }

    private fun setUpNextBusiness() {
        val c = Calendar.getInstance()
        c.timeInMillis = mNextWorkTime
        mHour = c.get(Calendar.HOUR_OF_DAY)
        mMinute = c.get(Calendar.MINUTE)
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH)
        mDay = c.get(Calendar.DAY_OF_MONTH)
    }

    private fun setUpTomorrow() {
        val c = Calendar.getInstance()
        c.timeInMillis = mTomorrowTime
        mHour = c.get(Calendar.HOUR_OF_DAY)
        mMinute = c.get(Calendar.MINUTE)
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH)
        mDay = c.get(Calendar.DAY_OF_MONTH)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_follow_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                saveDateTask()
                true
            }
            android.R.id.home -> {
                closeWindow()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
