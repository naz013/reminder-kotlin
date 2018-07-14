package com.elementary.tasks.core.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.views.roboto.RoboEditText
import com.elementary.tasks.core.views.roboto.RoboTextView

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
class RepeatView : LinearLayout, TextWatcher {

    private val seconds = 0
    private val minutes = 1
    private val hours = 2
    private val days = 3
    private val weeks = 4

    private var mPredictionView: LinearLayout? = null
    private var mEventView: RoboTextView? = null
    private var mRepeatInput: RoboEditText? = null
    private var mRepeatListener: OnRepeatListener? = null
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

    private val multiplier: Long
        get() {
            if (mState == seconds)
                return TimeCount.SECOND
            else if (mState == minutes)
                return TimeCount.MINUTE
            else if (mState == hours)
                return TimeCount.HOUR
            else if (mState == days)
                return TimeCount.DAY
            else if (mState == weeks) return TimeCount.DAY * 7
            return TimeCount.DAY
        }

    var repeat: Long
        get() {
            val rep = mRepeatValue * multiplier
            LogUtil.d(TAG, "getRepeat: $rep")
            return rep
        }
        set(mills) {
            if (mills == 0L) {
                setProgress(0)
                return
            }
            when {
                mills % (TimeCount.DAY * 7) == 0L -> {
                    val progress = mills / (TimeCount.DAY * 7)
                    setProgress(progress.toInt())
                    setState(weeks)
                }
                mills % TimeCount.DAY == 0L -> {
                    val progress = mills / TimeCount.DAY
                    setProgress(progress.toInt())
                    setState(days)
                }
                mills % TimeCount.HOUR == 0L -> {
                    val progress = mills / TimeCount.HOUR
                    setProgress(progress.toInt())
                    setState(hours)
                }
                mills % TimeCount.MINUTE == 0L -> {
                    val progress = mills / TimeCount.MINUTE
                    setProgress(progress.toInt())
                    setState(minutes)
                }
                mills % TimeCount.SECOND == 0L -> {
                    val progress = mills / TimeCount.SECOND
                    setProgress(progress.toInt())
                    setState(seconds)
                }
            }
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
        mRepeatInput = findViewById(R.id.repeatTitle)
        mEventView = findViewById(R.id.eventView)
        mPredictionView = findViewById(R.id.predictionView)
        val mRepeatType = findViewById<Spinner>(R.id.repeatType)
        mRepeatType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                setState(i)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }
        mRepeatInput!!.addTextChangedListener(this)
        mRepeatInput!!.setOnFocusChangeListener { _, hasFocus ->
            if (mImm == null) return@setOnFocusChangeListener
            if (!hasFocus) {
                mImm!!.hideSoftInputFromWindow(mRepeatInput!!.windowToken, 0)
            } else {
                mImm!!.showSoftInput(mRepeatInput, 0)
            }
        }
        mRepeatInput!!.setOnClickListener {
            if (mImm == null) return@setOnClickListener
            if (!mImm!!.isActive(mRepeatInput)) {
                mImm!!.showSoftInput(mRepeatInput, 0)
            }
        }
        mRepeatValue = 0
        mRepeatInput!!.setText(mRepeatValue.toString())
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
        mRepeatType.setSelection(mState)
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
        val is24 = Prefs.getInstance(context).is24HourFormatEnabled
        if (showPrediction) {
            mPredictionView!!.visibility = View.VISIBLE
            mEventView!!.text = TimeUtil.getFullDateTime(calendar.timeInMillis + progress * multiplier, is24, false)
        } else {
            mPredictionView!!.visibility = View.INVISIBLE
        }
    }

    fun enablePrediction(enable: Boolean) {
        if (enable) {
            mPredictionView!!.visibility = View.VISIBLE
        } else {
            mPredictionView!!.visibility = View.INVISIBLE
        }
        this.showPrediction = enable
    }

    private fun setState(state: Int) {
        this.mState = state
        updatePrediction(mRepeatValue)
    }

    fun setListener(listener: OnRepeatListener?) {
        this.mRepeatListener = listener
    }

    private fun updateEditField() {
        mRepeatInput!!.setSelection(mRepeatInput!!.text!!.length)
    }

    private fun setProgress(i: Int) {
        mRepeatValue = i
        mRepeatInput!!.setText(i.toString())
        updateEditField()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        try {
            mRepeatValue = Integer.parseInt(s.toString())
            if (mRepeatListener != null) mRepeatListener!!.onProgress(mRepeatValue)
        } catch (e: NumberFormatException) {
            mRepeatInput!!.setText("0")
        }

    }

    override fun afterTextChanged(s: Editable) {

    }

    interface OnRepeatListener {
        fun onProgress(progress: Int)
    }

    companion object {

        private const val TAG = "RepeatView"
    }
}
