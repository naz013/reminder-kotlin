package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.EditText
import android.widget.Spinner
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class BeforePickerViewBinding(view: View) : Binding(view) {
  val beforeTypeView: Spinner by bindView(R.id.beforeTypeView)
  val beforeValueView: EditText by bindView(R.id.beforeValueView)
  val hintIcon: View by bindView(R.id.hintIcon)
}