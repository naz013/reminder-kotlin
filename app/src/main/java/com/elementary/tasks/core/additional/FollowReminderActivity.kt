package com.elementary.tasks.core.additional

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.BindingActivity
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel
import com.elementary.tasks.databinding.ActivityFollowBinding
import java.util.*

class FollowReminderActivity : BindingActivity<ActivityFollowBinding>(R.layout.activity_follow), CompoundButton.OnCheckedChangeListener {

    private lateinit var viewModel: ReminderViewModel

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
    private var mNumber: String = ""
    private var canExportToTasks: Boolean = false
    private var defGroup: ReminderGroup? = null

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

    private var mDateCallBack: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        mCustomYear = year
        mCustomMonth = monthOfYear
        mCustomDay = dayOfMonth

        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, monthOfYear)
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        binding.customDate.text = TimeUtil.date(prefs.appLanguage).format(c.time)
    }

    private var mTimeCallBack: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        mCustomHour = hourOfDay
        mCustomMinute = minute

        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)

        binding.customTime.text = TimeUtil.getTime(c.time, mIs24Hour, prefs.appLanguage)
    }

    private val type: Int
        get() = if (binding.typeCall.isChecked)
            Reminder.BY_DATE_CALL
        else
            Reminder.BY_DATE_SMS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        canExportToTasks = GTasks.getInstance(this)?.isLogged ?: false
        val receivedDate = intent.getLongExtra(Constants.SELECTED_TIME, 0)
        mNumber = intent.getStringExtra(Constants.SELECTED_CONTACT_NUMBER) ?: ""
        val name = Contacts.getNameFromNumber(mNumber, this)

        val c = Calendar.getInstance()
        if (receivedDate != 0L) {
            c.timeInMillis = receivedDate
        } else {
            c.timeInMillis = System.currentTimeMillis()
        }
        mCurrentTime = c.timeInMillis

        binding.textField.hint = getString(R.string.message)

        if (name != null && !name.matches("".toRegex())) {
            binding.contactInfo.text = SuperUtil.appendString(name, "\n", mNumber)
        } else {
            binding.contactInfo.text = mNumber
        }

        val photo = Contacts.getPhoto(Contacts.getIdFromNumber(mNumber, this))
        if (photo != null) {
            binding.contactPhoto.show()
            binding.contactPhoto.setImageURI(photo)
        } else {
            binding.contactPhoto.hide()
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
        viewModel = ViewModelProviders.of(this, ReminderViewModel.Factory("")).get(ReminderViewModel::class.java)
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED -> closeWindow()
                    else -> {
                    }
                }
            }
        })
        viewModel.defaultReminderGroup.observe(this, Observer{
            defGroup = it
        })
    }

    private fun initViews() {
        binding.fab.setOnClickListener { saveDateTask() }
        binding.typeCall.isChecked = true
        binding.timeTomorrow.setOnCheckedChangeListener(this)
        binding.timeAfter.setOnCheckedChangeListener(this)
        binding.timeCustom.setOnCheckedChangeListener(this)
        binding.timeNextWorking.setOnCheckedChangeListener(this)
        binding.timeTomorrow.isChecked = true
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
        binding.nextWorkingTime.text = TimeUtil.getDateTime(c.time, mIs24Hour, prefs.appLanguage)
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
        binding.tomorrowTime.text = TimeUtil.getDateTime(c.time, mIs24Hour, prefs.appLanguage)
    }

    private fun initSpinner() {
        binding.afterTime.adapter = adapter
    }

    private fun initCustomTime() {
        val c = Calendar.getInstance()
        c.timeInMillis = mCurrentTime
        binding.customDate.text = TimeUtil.date(prefs.appLanguage).format(c.time)
        binding.customTime.text = TimeUtil.getTime(c.time, mIs24Hour, prefs.appLanguage)
        mCustomHour = c.get(Calendar.HOUR_OF_DAY)
        mCustomMinute = c.get(Calendar.MINUTE)
        mCustomYear = c.get(Calendar.YEAR)
        mCustomMonth = c.get(Calendar.MONTH)
        mCustomDay = c.get(Calendar.DAY_OF_MONTH)
        binding.customDate.setOnClickListener {
            binding.timeCustom.isChecked = true
            dateDialog()
        }
        binding.customTime.setOnClickListener {
            binding.timeCustom.isChecked = true
            timeDialog()
        }
    }

    private fun initExportChecks() {
        if (mCalendar || mStock) {
            binding.exportCheck.show()
        } else {
            binding.exportCheck.hide()
        }
        if (canExportToTasks) {
            binding.taskExport.show()
        } else {
            binding.taskExport.hide()
        }
    }

    private fun initPrefs() {
        mCalendar = prefs.isCalendarEnabled
        mStock = prefs.isStockCalendarEnabled
        mIs24Hour = prefs.is24HourFormat
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
        TimeUtil.showDatePicker(this, themeUtil.dialogStyle, prefs, mYear, mMonth, mDay, mDateCallBack)
    }

    private fun timeDialog() {
        TimeUtil.showTimePicker(this, themeUtil.dialogStyle, prefs.is24HourFormat, mCustomHour, mCustomMinute, mTimeCallBack)
    }

    private fun saveDateTask() {
        val text = binding.textField.text.toString().trim()
        if (text == "") {
            binding.textLayout.error = getString(R.string.must_be_not_empty)
            binding.textLayout.isErrorEnabled = true
            return
        }
        val type = type
        setUpTimes()
        val due = ReminderUtils.getTime(mDay, mMonth, mYear, mHour, mMinute, 0)
        if (!TimeCount.isCurrent(due)) {
            Toast.makeText(this, getString(R.string.select_date_in_future), Toast.LENGTH_SHORT).show()
            return
        }

        val reminder = Reminder()
        val def = defGroup
        if (def != null) {
            reminder.groupUuId = def.groupUuId
        }
        reminder.eventTime = TimeUtil.getGmtFromDateTime(due)
        reminder.startTime = TimeUtil.getGmtFromDateTime(due)
        reminder.type = type
        reminder.summary = text
        reminder.target = mNumber
        if (binding.taskExport.isVisible()) {
            reminder.exportToTasks = binding.taskExport.isChecked
        }
        if (binding.exportCheck.isVisible()) {
            reminder.exportToCalendar = binding.exportCheck.isChecked
        }
        viewModel.saveAndStartReminder(reminder)
    }

    private fun closeWindow() {
        removeFlags()
        finish()
    }

    private fun setUpTimes() {
        when {
            binding.timeNextWorking.isChecked -> setUpNextBusiness()
            binding.timeTomorrow.isChecked -> setUpTomorrow()
            binding.timeCustom.isChecked -> {
                mDay = mCustomDay
                mHour = mCustomHour
                mMinute = mCustomMinute
                mMonth = mCustomMonth
                mYear = mCustomYear
            }
            else -> {
                val c = Calendar.getInstance()
                c.timeInMillis = mCurrentTime + 1000 * 60 * getAfterMins(binding.afterTime.selectedItemPosition)
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
                if (binding.timeTomorrow.isChecked) {
                    binding.timeNextWorking.isChecked = false
                    binding.timeAfter.isChecked = false
                    binding.timeCustom.isChecked = false
                }
                setUpTomorrow()
            }
            R.id.timeNextWorking -> {
                if (binding.timeNextWorking.isChecked) {
                    binding.timeTomorrow.isChecked = false
                    binding.timeAfter.isChecked = false
                    binding.timeCustom.isChecked = false
                }
                setUpNextBusiness()
            }
            R.id.timeAfter -> if (binding.timeAfter.isChecked) {
                binding.timeTomorrow.isChecked = false
                binding.timeNextWorking.isChecked = false
                binding.timeCustom.isChecked = false
            }
            R.id.timeCustom -> if (binding.timeCustom.isChecked) {
                binding.timeTomorrow.isChecked = false
                binding.timeNextWorking.isChecked = false
                binding.timeAfter.isChecked = false
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

    override fun onBackPressed() {
        closeWindow()
    }

    companion object {

        fun mockScreen(context: Context, number: String, dataTime: Long) {
            context.startActivity(Intent(context, FollowReminderActivity::class.java)
                    .putExtra(Constants.SELECTED_CONTACT_NUMBER, number)
                    .putExtra(Constants.SELECTED_TIME, dataTime)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
    }
}
