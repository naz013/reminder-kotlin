package com.github.naz013.ui.common.view

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.github.naz013.ui.common.R

internal class PinCodeViewBinding(view: View) : Binding(view) {
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
