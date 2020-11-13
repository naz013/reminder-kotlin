package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class TimerPickerViewBinding(view: View) : Binding(view) {
  val hours: TextView by bindView(R.id.hours)
  val minutes: TextView by bindView(R.id.minutes)
  val seconds: TextView by bindView(R.id.seconds)
  val hoursView: TextView by bindView(R.id.hoursView)
  val minutesView: TextView by bindView(R.id.minutesView)
  val secondsView: TextView by bindView(R.id.secondsView)
  val deleteButton: View by bindView(R.id.deleteButton)
  val b1: View by bindView(R.id.b1)
  val b2: View by bindView(R.id.b2)
  val b3: View by bindView(R.id.b3)
  val b4: View by bindView(R.id.b4)
  val b5: View by bindView(R.id.b5)
  val b6: View by bindView(R.id.b6)
  val b7: View by bindView(R.id.b7)
  val b8: View by bindView(R.id.b8)
  val b9: View by bindView(R.id.b9)
  val b0: View by bindView(R.id.b0)
}