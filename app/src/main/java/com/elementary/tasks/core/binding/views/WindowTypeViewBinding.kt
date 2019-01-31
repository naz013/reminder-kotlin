package com.elementary.tasks.core.binding.views

import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class WindowTypeViewBinding(view: View) : Binding(view) {
    val chipGroup: ChipGroup by bindView(R.id.chipGroup)
    val chipFullscreen: Chip by bindView(R.id.chipFullscreen)
    val chipSimple: Chip by bindView(R.id.chipSimple)
    val hintIcon: View by bindView(R.id.hintIcon)
}