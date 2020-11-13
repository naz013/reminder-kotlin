package com.elementary.tasks.core.binding.views

import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class PriorityViewBinding(view: View) : Binding(view) {
  val chipGroup: ChipGroup by bindView(R.id.chipGroup)
  val chipLowest: Chip by bindView(R.id.chipLowest)
  val chipLow: Chip by bindView(R.id.chipLow)
  val chipNormal: Chip by bindView(R.id.chipNormal)
  val chipHigh: Chip by bindView(R.id.chipHigh)
  val chipHighest: Chip by bindView(R.id.chipHighest)
  val hintIcon: View by bindView(R.id.hintIcon)
}