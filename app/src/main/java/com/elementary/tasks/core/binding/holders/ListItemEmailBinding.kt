package com.elementary.tasks.core.binding.holders

import android.view.View
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class ListItemEmailBinding(view: View) : Binding(view) {
    val nameView: TextView by bindView(R.id.nameView)
    val emailView: TextView by bindView(R.id.emailView)
}