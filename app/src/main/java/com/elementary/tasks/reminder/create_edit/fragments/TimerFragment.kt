package com.elementary.tasks.reminder.create_edit.fragments

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ToggleButton

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.views.roboto.RoboTextView
import com.elementary.tasks.databinding.DialogExclusionPickerBinding
import com.elementary.tasks.databinding.FragmentTimerBinding
import com.elementary.tasks.core.data.models.Reminder

import java.util.ArrayList
import java.util.Calendar

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

class TimerFragment : RepeatableTypeFragment() {

    private var binding: FragmentTimerBinding? = null

    private var mHours: MutableList<Int>? = ArrayList()
    private var mFrom: String? = null
    private var mTo: String? = null
    private var fromHour: Int = 0
    private var fromMinute: Int = 0
    private var toHour: Int = 0
    private var toMinute: Int = 0
    private var buttons: ArrayList<ToggleButton>? = null

    private val selectedList: MutableList<Int>
        @SuppressLint("ResourceType")
        get() {
            val ids = ArrayList<Int>()
            for (button in buttons!!) {
                if (button.isChecked) ids.add(button.id - 100)
            }
            return ids
        }

    private val customizationView: DialogExclusionPickerBinding
        get() {
            val binding = DialogExclusionPickerBinding.inflate(LayoutInflater.from(context))
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
            binding.from.setOnClickListener { v -> fromTime(binding.from) }
            binding.to.setOnClickListener { v -> toTime(binding.to) }
            initButtons(binding)
            if (mFrom != null && mTo != null) {
                calendar.time = TimeUtil.getDate(mFrom)
                fromHour = calendar.get(Calendar.HOUR_OF_DAY)
                fromMinute = calendar.get(Calendar.MINUTE)
                calendar.time = TimeUtil.getDate(mTo)
                toHour = calendar.get(Calendar.HOUR_OF_DAY)
                toMinute = calendar.get(Calendar.MINUTE)
                binding.selectInterval.isChecked = true
            }
            if (mHours != null && mHours!!.size > 0) {
                binding.selectHours.isChecked = true
            }
            return binding
        }

    override fun prepare(): Reminder? {
        if (`interface` == null) return null
        val after = binding!!.timerPickerView.timerValue
        if (after == 0L) {
            Toast.makeText(context, getString(R.string.you_dont_insert_timer_time), Toast.LENGTH_SHORT).show()
            return null
        }
        var reminder: Reminder? = `interface`!!.reminder
        if (reminder == null) {
            reminder = Reminder()
        }

        val type = Reminder.BY_TIME
        reminder.type = type
        reminder.after = after
        val repeat = binding!!.repeatView.repeat
        reminder.repeatInterval = repeat
        reminder.isExportToCalendar = binding!!.exportToCalendar.isChecked
        reminder.isExportToTasks = binding!!.exportToTasks.isChecked
        reminder.from = mFrom
        reminder.to = mTo
        reminder.hours = mHours
        reminder.setClear(`interface`)
        val startTime = TimeCount.getInstance(context).generateNextTimer(reminder, true)
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
        inflater!!.inflate(R.menu.fragment_date_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_limit -> changeLimit()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTimerBinding.inflate(inflater, container, false)
        binding!!.timerPickerView.setListener(binding!!.repeatView.timerListener)
        `interface`!!.setExclusionAction { view -> openExclusionDialog() }
        if (`interface`!!.isExportToCalendar) {
            binding!!.exportToCalendar.visibility = View.VISIBLE
        } else {
            binding!!.exportToCalendar.visibility = View.GONE
        }
        if (`interface`!!.isExportToTasks) {
            binding!!.exportToTasks.visibility = View.VISIBLE
        } else {
            binding!!.exportToTasks.visibility = View.GONE
        }
        editReminder()
        return binding!!.root
    }

    private fun editReminder() {
        if (`interface`!!.reminder == null) return
        val reminder = `interface`!!.reminder
        binding!!.exportToCalendar.isChecked = reminder.isExportToCalendar
        binding!!.exportToTasks.isChecked = reminder.isExportToTasks
        binding!!.repeatView.repeat = reminder.repeatInterval
        binding!!.timerPickerView.timerValue = reminder.after
        this.mFrom = reminder.from
        this.mTo = reminder.to
        this.mHours = reminder.hours
    }

    private fun openExclusionDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.exclusion)
        val b = customizationView
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialog, which -> saveExclusion(b) }
        builder.setNegativeButton(R.string.remove_exclusion) { dialogInterface, i -> clearExclusion() }
        builder.create().show()
    }

    private fun clearExclusion() {
        mHours!!.clear()
        mFrom = null
        mTo = null
    }

    private fun saveExclusion(b: DialogExclusionPickerBinding) {
        clearExclusion()
        if (b.selectHours.isChecked) {
            mHours = selectedList
            if (mHours!!.size == 0) {
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

    private fun initButtons(b: DialogExclusionPickerBinding) {
        setId(b.zero, b.one, b.two, b.three, b.four, b.five, b.six, b.seven, b.eight, b.nine, b.ten,
                b.eleven, b.twelve, b.thirteen, b.fourteen, b.fifteen, b.sixteen, b.seventeen,
                b.eighteen, b.nineteen, b.twenty, b.twentyOne, b.twentyThree, b.twentyTwo)
    }

    private fun setId(vararg buttons: ToggleButton) {
        var i = 100
        val cs = ThemeUtil.getInstance(context)
        this.buttons = ArrayList()
        val selected = ArrayList(mHours!!)
        for (button in buttons) {
            button.id = i
            button.setBackgroundDrawable(cs.toggleDrawable())
            this.buttons!!.add(button)
            if (selected.contains(i - 100)) button.isChecked = true
            i++
        }
    }

    private fun fromTime(textView: RoboTextView) {
        TimeUtil.showTimePicker(context, { view, hourOfDay, minute ->
            fromHour = hourOfDay
            fromMinute = minute
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            textView.text = getString(R.string.from) + " " + TimeUtil.getTime(calendar.time, true)
        }, fromHour, fromMinute)
    }

    private fun toTime(textView: RoboTextView) {
        TimeUtil.showTimePicker(context, { view, hourOfDay, minute ->
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

        private val TAG = "TimerFragment"
    }
}
