package com.elementary.tasks.core.views

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeUtil
import kotlinx.android.synthetic.main.dialog_exclusion_picker.view.*
import kotlinx.android.synthetic.main.view_exclusion_picker.view.*
import java.util.*

/**
 * Copyright 2018 Nazar Suhovich
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
class ExclusionPickerView : LinearLayout {

    var onExclusionUpdateListener: ((hours: List<Int>, from: String, to: String) -> Unit)? = null
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
            binding.from.text = context.getString(R.string.from) + " " + TimeUtil.getTime(calendar.time, true, lang())
            calendar.timeInMillis = calendar.timeInMillis + AlarmManager.INTERVAL_HOUR * 3
            toHour = calendar.get(Calendar.HOUR_OF_DAY)
            toMinute = calendar.get(Calendar.MINUTE)
            binding.to.text = context.getString(R.string.to) + " " + TimeUtil.getTime(calendar.time, true, lang())
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

    var dialogues: Dialogues?  = null
    var prefs: Prefs? = null
    var themeUtil: ThemeUtil? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun lang(): Int = prefs?.appLanguage ?: 0

    fun setRangeHours(fromHour: String, toHour: String) {
        mFrom = fromHour
        mTo = toHour
        showRange()
    }

    fun setHours(hours: List<Int>) {
        mHours = hours.toMutableList()
        showHours()
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_exclusion_picker, this)
        orientation = LinearLayout.VERTICAL
        text.setOnClickListener {
            openExclusionDialog()
        }
        hintIcon.setOnLongClickListener {
            Toast.makeText(context, context.getString(R.string.exclusion), Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
    }

    private fun openExclusionDialog() {
        val dialogues = dialogues ?: return
        val builder = dialogues.getDialog(context)
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
        onExclusionUpdateListener?.invoke(mHours, mFrom, mTo)
    }

    private fun saveExclusion(b: View) {
        when {
            b.selectHours.isChecked -> {
                mHours = selectedList
                showHours()
                onExclusionUpdateListener?.invoke(mHours, mFrom, mTo)
            }
            b.selectInterval.isChecked -> {
                mFrom = getHour(fromHour, fromMinute)
                mTo = getHour(toHour, toMinute)
                showRange()
                onExclusionUpdateListener?.invoke(mHours, mFrom, mTo)
            }
            else -> {
                clearExclusion()
                showNoExclusion()
            }
        }
    }

    private fun showNoExclusion() {
        if (mHours.isEmpty() && mFrom == "" && mTo == "") {
            text.text = context.getString(R.string.not_selected)
        }
    }

    private fun showRange() {
        if (mFrom != "" && mTo != "") {
            var message = context.getString(R.string.from) + " "
            message += "$mFrom "
            message += context.getString(R.string.to) + " "
            message += mTo
            text.text = message
        } else {
            showNoExclusion()
        }
    }

    private fun showHours() {
        if (!mHours.isEmpty()) {
            val message = mHours.joinToString(separator = ", ")
            text.text = message
        } else {
            showNoExclusion()
        }
    }

    private fun getHour(hour: Int, minute: Int): String {
        return "$hour:$minute"
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
            button.setBackgroundResource(R.drawable.toggle_blue)
            this.buttons.add(button)
            if (selected.contains(i - 100)) button.isChecked = true
            i++
        }
    }

    private fun fromTime(textView: TextView) {
        val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            fromHour = hourOfDay
            fromMinute = minute
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            textView.text = context.getString(R.string.from) + " " + TimeUtil.getTime(calendar.time, true, lang())
        }
        val themeUtil = themeUtil
        if (themeUtil != null) {
            TimeUtil.showTimePicker(context!!, themeUtil.dialogStyle, prefs?.is24HourFormat ?: false, fromHour, fromMinute, listener)
        } else {
            TimeUtil.showTimePicker(context!!, prefs?.is24HourFormat ?: false, listener, fromHour, fromMinute)
        }
    }

    private fun toTime(textView: TextView) {
        val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            toHour = hourOfDay
            toMinute = minute
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            textView.text = context.getString(R.string.to) + " " + TimeUtil.getTime(calendar.time, true, lang())
        }
        val themeUtil = themeUtil
        if (themeUtil != null) {
            TimeUtil.showTimePicker(context!!, themeUtil.dialogStyle, prefs?.is24HourFormat ?: false, toHour, toMinute, listener)
        } else {
            TimeUtil.showTimePicker(context!!, prefs?.is24HourFormat ?: false, listener, toHour, toMinute)
        }
    }
}