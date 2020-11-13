package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class LedPickerViewBinding(view: View) : Binding(view) {
  val hintIcon: View by bindView(R.id.hintIcon)
  val ledGroup: RadioGroup by bindView(R.id.ledGroup)
  val ledRed: RadioButton by bindView(R.id.ledRed)
  val ledGreen: RadioButton by bindView(R.id.ledGreen)
  val ledBlue: RadioButton by bindView(R.id.ledBlue)
  val ledYellow: RadioButton by bindView(R.id.ledYellow)
  val ledPink: RadioButton by bindView(R.id.ledPink)
  val ledOrange: RadioButton by bindView(R.id.ledOrange)
  val ledTeal: RadioButton by bindView(R.id.ledTeal)
}