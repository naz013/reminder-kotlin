package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.views.roboto.RoboButton
import com.elementary.tasks.core.views.roboto.RoboTextView

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

    private var hoursView: RoboTextView? = null
    private var minutesView: RoboTextView? = null
    private var secondsView: RoboTextView? = null
    private var deleteButton: ThemedImageButton? = null

    private var timeString = "000000"

    private var mListener: TimerListener? = null

    var timerValue: Long
        get() = SuperUtil.getAfterTime(timeString)
        set(mills) {
            timeString = TimeUtil.generateAfterString(mills)
            updateTimeView()
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
        orientation = LinearLayout.VERTICAL
        View.inflate(context, R.layout.view_timer_picker, this)
        hoursView = findViewById(R.id.hoursView)
        minutesView = findViewById(R.id.minutesView)
        secondsView = findViewById(R.id.secondsView)
        deleteButton = findViewById(R.id.deleteButton)
        deleteButton!!.setOnClickListener {
            timeString = timeString.substring(0, timeString.length - 1)
            timeString = "0$timeString"
            updateTimeView()
        }
        deleteButton!!.setOnLongClickListener {
            timeString = "000000"
            updateTimeView()
            true
        }
        initButtons()
        updateTimeView()
    }

    private fun initButtons() {
        val b1 = findViewById<RoboButton>(R.id.b1)
        val b2 = findViewById<RoboButton>(R.id.b2)
        val b3 = findViewById<RoboButton>(R.id.b3)
        val b4 = findViewById<RoboButton>(R.id.b4)
        val b5 = findViewById<RoboButton>(R.id.b5)
        val b6 = findViewById<RoboButton>(R.id.b6)
        val b7 = findViewById<RoboButton>(R.id.b7)
        val b8 = findViewById<RoboButton>(R.id.b8)
        val b9 = findViewById<RoboButton>(R.id.b9)
        val b0 = findViewById<RoboButton>(R.id.b0)
        if (b1 != null) {
            b1.id = Integer.valueOf(101)!!
            b2.id = Integer.valueOf(102)!!
            b3.id = Integer.valueOf(103)!!
            b4.id = Integer.valueOf(104)!!
            b5.id = Integer.valueOf(105)!!
            b6.id = Integer.valueOf(106)!!
            b7.id = Integer.valueOf(107)!!
            b8.id = Integer.valueOf(108)!!
            b9.id = Integer.valueOf(109)!!
            b0.id = Integer.valueOf(100)!!
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
        deleteButton!!.isEnabled = !timeString.matches("000000".toRegex())
        if (timeString.length == 6) {
            val hours = timeString.substring(0, 2)
            val minutes = timeString.substring(2, 4)
            val seconds = timeString.substring(4, 6)
            hoursView!!.text = hours
            minutesView!!.text = minutes
            secondsView!!.text = seconds
            if (mListener != null) mListener!!.onTimerChange(timerValue)
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
