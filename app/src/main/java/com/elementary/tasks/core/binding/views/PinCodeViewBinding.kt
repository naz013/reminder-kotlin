package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class PinCodeViewBinding(view: View) : Binding(view) {
    val birdsView: LinearLayout by bindView(R.id.birdsView)
    val deleteButton: View by bindView(R.id.deleteButton)
    val b1: TextView by bindView(R.id.b1)
    val b2: TextView by bindView(R.id.b2)
    val b3: TextView by bindView(R.id.b3)
    val b4: TextView by bindView(R.id.b4)
    val b5: TextView by bindView(R.id.b5)
    val b6: TextView by bindView(R.id.b6)
    val b7: TextView by bindView(R.id.b7)
    val b8: TextView by bindView(R.id.b8)
    val b9: TextView by bindView(R.id.b9)
    val b0: TextView by bindView(R.id.b0)
}