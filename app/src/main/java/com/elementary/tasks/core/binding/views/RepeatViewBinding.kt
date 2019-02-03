package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class RepeatViewBinding(view: View) : Binding(view) {
    val repeatType: Spinner by bindView(R.id.repeatType)
    val text1: TextView by bindView(R.id.text1)
    val repeatTitle: EditText by bindView(R.id.repeatTitle)
    val hintIcon: View by bindView(R.id.hintIcon)
}