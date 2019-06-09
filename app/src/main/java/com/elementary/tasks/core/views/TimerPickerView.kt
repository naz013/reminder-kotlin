package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.TimerPickerViewBinding
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeUtil

class TimerPickerView : LinearLayout, View.OnClickListener {

    private lateinit var binding: TimerPickerViewBinding
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
        orientation = VERTICAL
        View.inflate(context, R.layout.view_timer_picker, this)
        binding = TimerPickerViewBinding(this)

        binding.deleteButton.setOnClickListener {
            timeString = timeString.substring(0, timeString.length - 1)
            timeString = "0$timeString"
            updateTimeView()
        }
        binding.deleteButton.setOnLongClickListener {
            timeString = "000000"
            updateTimeView()
            true
        }
        initButtons()
        updateTimeView()
    }

    private fun initButtons() {
        binding.b1.id = Integer.valueOf(101)
        binding.b2.id = Integer.valueOf(102)
        binding.b3.id = Integer.valueOf(103)
        binding.b4.id = Integer.valueOf(104)
        binding.b5.id = Integer.valueOf(105)
        binding.b6.id = Integer.valueOf(106)
        binding.b7.id = Integer.valueOf(107)
        binding.b8.id = Integer.valueOf(108)
        binding.b9.id = Integer.valueOf(109)
        binding.b0.id = Integer.valueOf(100)
        binding.b1.setOnClickListener(this)
        binding.b2.setOnClickListener(this)
        binding.b3.setOnClickListener(this)
        binding.b4.setOnClickListener(this)
        binding.b5.setOnClickListener(this)
        binding.b6.setOnClickListener(this)
        binding.b7.setOnClickListener(this)
        binding.b8.setOnClickListener(this)
        binding.b9.setOnClickListener(this)
        binding.b0.setOnClickListener(this)
    }

    fun setListener(listener: TimerListener) {
        this.mListener = listener
    }

    private fun updateTimeView() {
        binding.deleteButton.isEnabled = !timeString.matches("000000".toRegex())
        if (timeString.length == 6) {
            val hours = timeString.substring(0, 2)
            val minutes = timeString.substring(2, 4)
            val seconds = timeString.substring(4, 6)
            binding.hoursView.text = hours
            binding.minutesView.text = minutes
            binding.secondsView.text = seconds
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
