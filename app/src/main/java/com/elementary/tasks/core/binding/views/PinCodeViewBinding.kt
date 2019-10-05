package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding
import com.google.android.material.card.MaterialCardView

class PinCodeViewBinding(view: View) : Binding(view) {
    val birdsView: LinearLayout by bindView(R.id.birdsView)
    val deleteButton: View by bindView(R.id.bC)
    val fingerButton: View by bindView(R.id.bF)

    val t1: TextView by bindView(R.id.t1)
    val t2: TextView by bindView(R.id.t2)
    val t3: TextView by bindView(R.id.t3)
    val t4: TextView by bindView(R.id.t4)
    val t5: TextView by bindView(R.id.t5)
    val t6: TextView by bindView(R.id.t6)
    val t7: TextView by bindView(R.id.t7)
    val t8: TextView by bindView(R.id.t8)
    val t9: TextView by bindView(R.id.t9)
    val t0: TextView by bindView(R.id.t0)

    val b1: MaterialCardView by bindView(R.id.b1)
    val b2: MaterialCardView by bindView(R.id.b2)
    val b3: MaterialCardView by bindView(R.id.b3)
    val b4: MaterialCardView by bindView(R.id.b4)
    val b5: MaterialCardView by bindView(R.id.b5)
    val b6: MaterialCardView by bindView(R.id.b6)
    val b7: MaterialCardView by bindView(R.id.b7)
    val b8: MaterialCardView by bindView(R.id.b8)
    val b9: MaterialCardView by bindView(R.id.b9)
    val b0: MaterialCardView by bindView(R.id.b0)
}