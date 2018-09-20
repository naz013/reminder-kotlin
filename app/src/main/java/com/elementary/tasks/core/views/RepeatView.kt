package com.elementary.tasks.core.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import kotlinx.android.synthetic.main.view_repeat.view.*
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
class RepeatView : LinearLayout, TextWatcher {

    private val seconds = 0
    private val minutes = 1
    private val hours = 2
    private val days = 3
    private val weeks = 4

    private var mRepeatListener: OnRepeatListener? = null
    var onRepeatChangeListener: OnRepeatChangeListener? = null
    private var mImm: InputMethodManager? = null

    private var showPrediction = true
    private var mState = days
    private var mDay: Int = 0
    private var mMonth: Int = 0
    private var mYear: Int = 0
    private var mHour: Int = 0
    private var mMinute: Int = 0
    private var mRepeatValue: Int = 0

    val eventListener: DateTimeView.OnSelectListener = object : DateTimeView.OnSelectListener {
        override fun onDateSelect(mills: Long, day: Int, month: Int, year: Int) {
            mYear = year
            mMonth = month
            mDay = day
            updatePrediction(mRepeatValue)
        }

        override fun onTimeSelect(mills: Long, hour: Int, minute: Int) {
            mHour = hour
            mMinute = minute
            updatePrediction(mRepeatValue)
        }
    }
    val timerListener = object : TimerPickerView.TimerListener {
        override fun onTimerChange(time: Long) {
            initDateTime(System.currentTimeMillis() + time)
        }
    }

    @Inject
    lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
    }

    private val multiplier: Long
        get() {
            return when (mState) {
                seconds -> TimeCount.SECOND
                minutes -> TimeCount.MINUTE
                hours -> TimeCount.HOUR
                days -> TimeCount.DAY
                weeks -> TimeCount.DAY * 7
                else -> TimeCount.DAY
            }
        }

    var repeat: Long
        get() {
            return mRepeatValue * multiplier
        }
        set(mills) {
            if (mills == 0L) {
                setProgress(0)
                return
            }
            when {
                fitInterval(mills, TimeCount.DAY * 7) -> {
                    val progress = mills / (TimeCount.DAY * 7)
                    setProgress(progress.toInt())
                    repeatType.setSelection(weeks)
                }
                fitInterval(mills, TimeCount.DAY) -> {
                    val progress = mills / TimeCount.DAY
                    setProgress(progress.toInt())
                    repeatType.setSelection(days)
                }
                fitInterval(mills, TimeCount.HOUR) -> {
                    val progress = mills / TimeCount.HOUR
                    setProgress(progress.toInt())
                    repeatType.setSelection(hours)
                }
                fitInterval(mills, TimeCount.MINUTE) -> {
                    val progress = mills / TimeCount.MINUTE
                    setProgress(progress.toInt())
                    repeatType.setSelection(minutes)
                }
                fitInterval(mills, TimeCount.SECOND) -> {
                    val progress = mills / TimeCount.SECOND
                    setProgress(progress.toInt())
                    repeatType.setSelection(seconds)
                }
            }
        }

    private fun fitInterval(interval: Long, matcher: Long): Boolean {
        return interval > matcher && (interval % matcher == 0L)
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_repeat, this)
        orientation = LinearLayout.VERTICAL
        mImm = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        repeatType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                setState(i)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }
        repeatTitle.addTextChangedListener(this)
        repeatTitle.setOnFocusChangeListener { _, hasFocus ->
            if (mImm == null) return@setOnFocusChangeListener
            if (!hasFocus) {
                mImm?.hideSoftInputFromWindow(repeatTitle.windowToken, 0)
            } else {
                mImm?.showSoftInput(repeatTitle, 0)
            }
        }
        repeatTitle.setOnClickListener {
            if (mImm == null) return@setOnClickListener
            if (!mImm!!.isActive(repeatTitle)) {
                mImm?.showSoftInput(repeatTitle, 0)
            }
        }
        mRepeatValue = 0
        repeatTitle.setText(mRepeatValue.toString())
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.RepeatView, 0, 0)
            try {
                mState = a.getInt(R.styleable.RepeatView_repeat_type, days)
            } catch (e: Exception) {
                LogUtil.e(TAG, "There was an error loading attributes.", e)
            } finally {
                a.recycle()
            }
        }
        repeatType.setSelection(mState)
        initDateTime(System.currentTimeMillis())
    }

    fun initDateTime(time: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        mYear = cal.get(Calendar.YEAR)
        mMonth = cal.get(Calendar.MONTH)
        mDay = cal.get(Calendar.DAY_OF_MONTH)
        mHour = cal.get(Calendar.HOUR_OF_DAY)
        mMinute = cal.get(Calendar.MINUTE)
        updatePrediction(mRepeatValue)
    }

    fun setDateTime(dateTime: String?) {
        initDateTime(TimeUtil.getDateTimeFromGmt(dateTime))
    }

    private fun updatePrediction(progress: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(mYear, mMonth, mDay, mHour, mMinute, 0)
        val is24 = prefs.is24HourFormatEnabled
        if (showPrediction) {
            predictionView.visibility = View.VISIBLE
            eventView.text = TimeUtil.getFullDateTime(calendar.timeInMillis + progress * multiplier, is24, false)
        } else {
            predictionView.visibility = View.INVISIBLE
        }
        onRepeatChangeListener?.onChanged(repeat)
    }

    fun enablePrediction(enable: Boolean) {
        if (enable) {
            predictionView.visibility = View.VISIBLE
        } else {
            predictionView.visibility = View.INVISIBLE
        }
        this.showPrediction = enable
    }

    private fun setState(state: Int) {
        if (mState == state) return
        this.mState = state
        updatePrediction(mRepeatValue)
    }

    fun setListener(listener: OnRepeatListener?) {
        this.mRepeatListener = listener
    }

    private fun updateEditField() {
        repeatTitle.setSelection(repeatTitle.text.toString().length)
    }

    private fun setProgress(i: Int) {
        mRepeatValue = i
        repeatTitle.setText(i.toString())
        updateEditField()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        try {
            mRepeatValue = Integer.parseInt(s.toString())
            mRepeatListener?.onProgress(mRepeatValue)
            onRepeatChangeListener?.onChanged(repeat)
        } catch (e: NumberFormatException) {
            repeatTitle.setText("0")
        }
    }

    override fun afterTextChanged(s: Editable) {
    }

    interface OnRepeatListener {
        fun onProgress(progress: Int)
    }

    interface OnRepeatChangeListener {
        fun onChanged(repeat: Long)
    }

    companion object {
        private const val TAG = "RepeatView"
    }
}
