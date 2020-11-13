package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class RepeatLimitViewBinding(view: View) : Binding(view) {
  val sliderView: SeekBar by bindView(R.id.sliderView)
  val labelView: TextView by bindView(R.id.labelView)
  val hintIcon: View by bindView(R.id.hintIcon)
}