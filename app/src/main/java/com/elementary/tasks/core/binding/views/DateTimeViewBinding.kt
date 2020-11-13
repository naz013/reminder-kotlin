package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class DateTimeViewBinding(view: View) : Binding(view) {
  val dateField: TextView by bindView(R.id.dateField)
  val timeField: TextView by bindView(R.id.timeField)
}