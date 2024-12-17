package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.databinding.ViewTimerPickerBinding
import com.github.naz013.logging.Logger

class TimerPickerView : LinearLayout {

  private lateinit var binding: ViewTimerPickerBinding
  private var timeString = "000000"
  private var mListener: TimerListener? = null

  var timerValue: Long
    get() = SuperUtil.getAfterTime(timeString)
    set(mills) {
      timeString = DateTimeManager.generateViewAfterString(mills, divider = "")
      updateTimeView()
    }

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init(context)
  }

  private fun init(context: Context) {
    orientation = VERTICAL
    View.inflate(context, R.layout.view_timer_picker, this)
    binding = ViewTimerPickerBinding.bind(this)

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
    binding.b1.setOnClickListener { onDigitClicked(1) }
    binding.b2.setOnClickListener { onDigitClicked(2) }
    binding.b3.setOnClickListener { onDigitClicked(3) }
    binding.b4.setOnClickListener { onDigitClicked(4) }
    binding.b5.setOnClickListener { onDigitClicked(5) }
    binding.b6.setOnClickListener { onDigitClicked(6) }
    binding.b7.setOnClickListener { onDigitClicked(7) }
    binding.b8.setOnClickListener { onDigitClicked(8) }
    binding.b9.setOnClickListener { onDigitClicked(9) }
    binding.b0.setOnClickListener { onDigitClicked(0) }
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

  private fun onDigitClicked(d: Int) {
    Logger.d("onDigitClicked: $d, $timeString")
    if (timeString[0] == '0') {
      timeString = timeString.substring(1, timeString.length)
      timeString += d.toString()
      updateTimeView()
    }
  }

  interface TimerListener {
    fun onTimerChange(time: Long)
  }
}
