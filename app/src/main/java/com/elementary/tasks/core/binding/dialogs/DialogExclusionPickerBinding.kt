package com.elementary.tasks.core.binding.dialogs

import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.ToggleButton
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class DialogExclusionPickerBinding(view: View) : Binding(view) {
  val radioGroup: RadioGroup by bindView(R.id.radioGroup)
  val selectInterval: RadioButton by bindView(R.id.selectInterval)
  val selectHours: RadioButton by bindView(R.id.selectHours)
  val intervalContainer: View by bindView(R.id.intervalContainer)
  val hoursContainer: View by bindView(R.id.hoursContainer)
  val from: TextView by bindView(R.id.from)
  val to: TextView by bindView(R.id.to)

  val zero: ToggleButton by bindView(R.id.zero)
  val one: ToggleButton by bindView(R.id.one)
  val two: ToggleButton by bindView(R.id.two)
  val three: ToggleButton by bindView(R.id.three)
  val four: ToggleButton by bindView(R.id.four)
  val five: ToggleButton by bindView(R.id.five)
  val six: ToggleButton by bindView(R.id.six)
  val seven: ToggleButton by bindView(R.id.seven)
  val eight: ToggleButton by bindView(R.id.eight)
  val nine: ToggleButton by bindView(R.id.nine)
  val ten: ToggleButton by bindView(R.id.ten)
  val eleven: ToggleButton by bindView(R.id.eleven)
  val twelve: ToggleButton by bindView(R.id.twelve)
  val thirteen: ToggleButton by bindView(R.id.thirteen)
  val fourteen: ToggleButton by bindView(R.id.fourteen)
  val fifteen: ToggleButton by bindView(R.id.fifteen)
  val sixteen: ToggleButton by bindView(R.id.sixteen)
  val seventeen: ToggleButton by bindView(R.id.seventeen)
  val eighteen: ToggleButton by bindView(R.id.eighteen)
  val nineteen: ToggleButton by bindView(R.id.nineteen)
  val twenty: ToggleButton by bindView(R.id.twenty)
  val twentyOne: ToggleButton by bindView(R.id.twentyOne)
  val twentyTwo: ToggleButton by bindView(R.id.twentyTwo)
  val twentyThree: ToggleButton by bindView(R.id.twentyThree)
}