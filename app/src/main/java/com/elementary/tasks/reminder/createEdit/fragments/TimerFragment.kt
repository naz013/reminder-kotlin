package com.elementary.tasks.reminder.createEdit.fragments

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import kotlinx.android.synthetic.main.dialog_exclusion_picker.view.*
import kotlinx.android.synthetic.main.fragment_timer.*
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
@SuppressLint("SetTextI18n")
class TimerFragment : RepeatableTypeFragment() {

    private var mHours: MutableList<Int> = mutableListOf()
    private var mFrom: String = ""
    private var mTo: String = ""
    private var fromHour: Int = 0
    private var fromMinute: Int = 0
    private var toHour: Int = 0
    private var toMinute: Int = 0
    private var buttons: MutableList<ToggleButton> = mutableListOf()

    private val selectedList: MutableList<Int>
        @SuppressLint("ResourceType")
        get() {
            val ids = ArrayList<Int>()
            for (button in buttons) {
                if (button.isChecked) ids.add(button.id - 100)
            }
            return ids
        }

    private val customizationView: View
        get() {
            val binding = LayoutInflater.from(context).inflate(R.layout.dialog_exclusion_picker, null)
            binding.selectInterval.isChecked = true
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            fromHour = calendar.get(Calendar.HOUR_OF_DAY)
            fromMinute = calendar.get(Calendar.MINUTE)
            binding.from.text = getString(R.string.from) + " " + TimeUtil.getTime(calendar.time, true)
            calendar.timeInMillis = calendar.timeInMillis + AlarmManager.INTERVAL_HOUR * 3
            toHour = calendar.get(Calendar.HOUR_OF_DAY)
            toMinute = calendar.get(Calendar.MINUTE)
            binding.to.text = getString(R.string.to) + " " + TimeUtil.getTime(calendar.time, true)
            binding.from.setOnClickListener { fromTime(binding.from) }
            binding.to.setOnClickListener { toTime(binding.to) }
            initButtons(binding)
            if (mFrom != "" && mTo != "") {
                calendar.time = TimeUtil.getDate(mFrom)
                fromHour = calendar.get(Calendar.HOUR_OF_DAY)
                fromMinute = calendar.get(Calendar.MINUTE)
                calendar.time = TimeUtil.getDate(mTo)
                toHour = calendar.get(Calendar.HOUR_OF_DAY)
                toMinute = calendar.get(Calendar.MINUTE)
                binding.selectInterval.isChecked = true
            }
            if (!mHours.isEmpty()) {
                binding.selectHours.isChecked = true
            }
            return binding
        }

    override fun prepare(): Reminder? {
        val iFace = reminderInterface ?: return null
        val after = timerPickerView.timerValue
        if (after == 0L) {
            Toast.makeText(context, getString(R.string.you_dont_insert_timer_time), Toast.LENGTH_SHORT).show()
            return null
        }
        var reminder = iFace.reminder
        if (reminder == null) {
            reminder = Reminder()
        }
        val type = Reminder.BY_TIME
        reminder.type = type
        reminder.after = after
        val repeat = repeatView.repeat
        reminder.repeatInterval = repeat
        reminder.exportToCalendar = exportToCalendar.isChecked
        reminder.exportToTasks = exportToTasks.isChecked
        reminder.from = mFrom
        reminder.to = mTo
        reminder.hours = mHours
        reminder.setClear(iFace)
        val startTime = timeCount.generateNextTimer(reminder, true)
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
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timerPickerView.setListener(repeatView.timerListener)
        initScreenState()
        editReminder()
    }

    private fun initScreenState() {
        val iFace = reminderInterface ?: return
        iFace.setExclusionAction(View.OnClickListener { openExclusionDialog() })
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

    private fun editReminder() {
        val iFace = reminderInterface ?: return
        val reminder = iFace.reminder ?: return
        exportToCalendar.isChecked = reminder.exportToCalendar
        exportToTasks.isChecked = reminder.exportToTasks
        repeatView.repeat = reminder.repeatInterval
        timerPickerView.timerValue = reminder.after
        this.mFrom = reminder.from
        this.mTo = reminder.to
        this.mHours = reminder.hours.toMutableList()
    }

    private fun openExclusionDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(R.string.exclusion)
        val b = customizationView
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { _, _ -> saveExclusion(b) }
        builder.setNegativeButton(R.string.remove_exclusion) { _, _ -> clearExclusion() }
        builder.create().show()
    }

    private fun clearExclusion() {
        mHours.clear()
        mFrom = ""
        mTo = ""
    }

    private fun saveExclusion(b: View) {
        clearExclusion()
        if (b.selectHours.isChecked) {
            mHours = selectedList
            if (mHours.isEmpty()) {
                Toast.makeText(context, getString(R.string.you_dont_select_any_hours), Toast.LENGTH_SHORT).show()
            }
        } else if (b.selectInterval.isChecked) {
            mFrom = getHour(fromHour, fromMinute)
            mTo = getHour(toHour, toMinute)
        }
    }

    private fun getHour(hour: Int, minute: Int): String {
        return hour.toString() + ":" + minute
    }

    private fun initButtons(b: View) {
        setId(b.zero, b.one, b.two, b.three, b.four, b.five, b.six, b.seven, b.eight, b.nine, b.ten,
                b.eleven, b.twelve, b.thirteen, b.fourteen, b.fifteen, b.sixteen, b.seventeen,
                b.eighteen, b.nineteen, b.twenty, b.twentyOne, b.twentyThree, b.twentyTwo)
    }

    private fun setId(vararg buttons: ToggleButton) {
        var i = 100
        this.buttons = mutableListOf()
        val selected = ArrayList(mHours)
        for (button in buttons) {
            button.id = i
            button.setBackgroundDrawable(themeUtil.toggleDrawable())
            this.buttons.add(button)
            if (selected.contains(i - 100)) button.isChecked = true
            i++
        }
    }

    private fun fromTime(textView: TextView) {
        TimeUtil.showTimePicker(context!!, prefs.is24HourFormatEnabled, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            fromHour = hourOfDay
            fromMinute = minute
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            textView.text = getString(R.string.from) + " " + TimeUtil.getTime(calendar.time, true)
        }, fromHour, fromMinute)
    }

    private fun toTime(textView: TextView) {
        TimeUtil.showTimePicker(context!!, prefs.is24HourFormatEnabled, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            toHour = hourOfDay
            toMinute = minute
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            textView.text = getString(R.string.to) + " " + TimeUtil.getTime(calendar.time, true)
        }, toHour, toMinute)
    }

    companion object {

        private const val TAG = "TimerFragment"
    }
}
