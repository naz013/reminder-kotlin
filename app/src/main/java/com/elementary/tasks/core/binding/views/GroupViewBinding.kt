package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class GroupViewBinding(view: View) : Binding(view) {
  val hintIcon: View by bindView(R.id.hintIcon)
  val text: TextView by bindView(R.id.text)
}