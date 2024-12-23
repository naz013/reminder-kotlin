package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.elementary.tasks.R
import com.github.naz013.ui.common.view.Binding

class HorizontalSelectorViewBinding(view: View) : Binding(view) {
  val leftButton: ImageView by bindView(R.id.leftButton)
  val rightButton: ImageView by bindView(R.id.rightButton)
  val text1: TextView by bindView(R.id.text1)
}
