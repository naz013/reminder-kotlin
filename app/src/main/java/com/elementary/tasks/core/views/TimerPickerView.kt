package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeUtil
import kotlinx.android.synthetic.main.view_timer_picker.view.*

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

class TimerPickerView : LinearLayout, View.OnClickListener {

    private var timeString = "000000"

    private var mListener: TimerListener? = null

    var timerValue: Long
        get() = SuperUtil.getAfterTime(timeString)
        set(mills) {
            timeString = TimeUtil.generateAfterString(mills)
            updateTimeView()
        }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {
        orientation = LinearLayout.VERTICAL
        View.inflate(context, R.layout.view_timer_picker, this)
        deleteButton.setOnClickListener {
            timeString = timeString.substring(0, timeString.length - 1)
            timeString = "0$timeString"
            updateTimeView()
        }
        deleteButton.setOnLongClickListener {
            timeString = "000000"
            updateTimeView()
            true
        }
        initButtons()
        updateTimeView()
    }

    private fun initButtons() {
        if (b1 != null) {
            b1.id = Integer.valueOf(101)
            b2.id = Integer.valueOf(102)
            b3.id = Integer.valueOf(103)
            b4.id = Integer.valueOf(104)
            b5.id = Integer.valueOf(105)
            b6.id = Integer.valueOf(106)
            b7.id = Integer.valueOf(107)
            b8.id = Integer.valueOf(108)
            b9.id = Integer.valueOf(109)
            b0.id = Integer.valueOf(100)
            b1.setOnClickListener(this)
            b2.setOnClickListener(this)
            b3.setOnClickListener(this)
            b4.setOnClickListener(this)
            b5.setOnClickListener(this)
            b6.setOnClickListener(this)
            b7.setOnClickListener(this)
            b8.setOnClickListener(this)
            b9.setOnClickListener(this)
            b0.setOnClickListener(this)
        }
    }

    fun setListener(listener: TimerListener) {
        this.mListener = listener
    }

    private fun updateTimeView() {
        deleteButton.isEnabled = !timeString.matches("000000".toRegex())
        if (timeString.length == 6) {
            val hours = timeString.substring(0, 2)
            val minutes = timeString.substring(2, 4)
            val seconds = timeString.substring(4, 6)
            hoursView.text = hours
            minutesView.text = minutes
            secondsView.text = seconds
            mListener?.onTimerChange(timerValue)
        }
    }

    override fun onClick(view: View) {
        val ids = view.id
        if (ids in 100..109) {
            val charS = timeString[0].toString()
            if (charS.matches("0".toRegex())) {
                timeString = timeString.substring(1, timeString.length)
                timeString += (ids - 100).toString()
                updateTimeView()
            }
        }
    }

    interface TimerListener {
        fun onTimerChange(time: Long)
    }
}
