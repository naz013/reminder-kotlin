package com.elementary.tasks.core.views

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TimePicker
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeUtil
import kotlinx.android.synthetic.main.view_date_time.view.*
import java.text.DateFormat
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
class DateTimeView : LinearLayout, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private var mHour: Int = 0
    private var mMinute: Int = 0
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0
    private var isSingleMode = false
    private var mListener: OnSelectListener? = null
    private var mDateFormat: DateFormat = TimeUtil.FULL_DATE_FORMAT

    private val mDateClick = View.OnClickListener{ selectDate() }

    @Inject
    lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
    }

    var dateTime: Long
        get() {
            val calendar = Calendar.getInstance()
            calendar.set(mYear, mMonth, mDay, mHour, mMinute, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
        set(dateTime) = updateDateTime(dateTime)

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    fun setEventListener(listener: OnSelectListener) {
        mListener = listener
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        orientation = LinearLayout.VERTICAL
        View.inflate(context, R.layout.view_date_time, this)
        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams = params

        dateField.setOnClickListener(mDateClick)
        timeField.setOnClickListener { selectTime() }
        updateDateTime(0)
    }

    fun setDateFormat(format: DateFormat) {
        this.mDateFormat = format
        this.invalidate()
    }

    override fun setOnClickListener(l: View.OnClickListener?) {
        if (isSingleMode) dateField.setOnClickListener(l)
    }

    override fun setOnLongClickListener(l: View.OnLongClickListener?) {
        dateField.setOnLongClickListener(l)
        timeField.setOnLongClickListener(l)
    }

    fun setSingleText(text: String?) {
        isSingleMode = text != null
        if (!isSingleMode) {
            timeField.visibility = View.VISIBLE
            dateField.setOnClickListener(mDateClick)
            updateDateTime(0)
        } else {
            dateField.text = text
            dateField.setOnClickListener(null)
            timeField.visibility = View.GONE
        }
    }

    fun setDateTime(dateTime: String) {
        val mills = TimeUtil.getDateTimeFromGmt(dateTime)
        updateDateTime(mills)
    }

    private fun updateDateTime(mills: Long) {
        var milliseconds = mills
        if (milliseconds == 0L) {
            milliseconds = System.currentTimeMillis()
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
        mYear = calendar.get(Calendar.YEAR)
        mMonth = calendar.get(Calendar.MONTH)
        mDay = calendar.get(Calendar.DAY_OF_MONTH)
        mHour = calendar.get(Calendar.HOUR_OF_DAY)
        mMinute = calendar.get(Calendar.MINUTE)
        updateTime(milliseconds)
        updateDate(milliseconds)
    }

    private fun updateDate(mills: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = mills
        dateField.text = TimeUtil.getDate(cal.time, mDateFormat)
        mListener?.onDateSelect(mills, mDay, mMonth, mYear)
    }

    private fun updateTime(mills: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = mills
        timeField.text = TimeUtil.getTime(cal.time, prefs.is24HourFormatEnabled)
        mListener?.onTimeSelect(mills, mHour, mMinute)
    }

    private fun selectDate() {
        TimeUtil.showDatePicker(context, prefs, this, mYear, mMonth, mDay)
    }

    private fun selectTime() {
        TimeUtil.showTimePicker(context, prefs.is24HourFormatEnabled, this, mHour, mMinute)
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        this.mYear = year
        this.mMonth = monthOfYear
        this.mDay = dayOfMonth
        val cal = Calendar.getInstance()
        cal.set(year, monthOfYear, dayOfMonth)
        updateDate(cal.timeInMillis)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        this.mHour = hourOfDay
        this.mMinute = minute
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minute)
        updateTime(cal.timeInMillis)
    }

    interface OnSelectListener {
        fun onDateSelect(mills: Long, day: Int, month: Int, year: Int)

        fun onTimeSelect(mills: Long, hour: Int, minute: Int)
    }
}
