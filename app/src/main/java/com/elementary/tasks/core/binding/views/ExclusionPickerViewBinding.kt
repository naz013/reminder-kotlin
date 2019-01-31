package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class ExclusionPickerViewBinding(view: View) : Binding(view) {
    val text: TextView by bindView(R.id.text)
    val hintIcon: View by bindView(R.id.hintIcon)
}