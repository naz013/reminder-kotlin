package com.elementary.tasks.core.binding.views

import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding
import com.google.android.material.chip.Chip

class ChipViewBinding(view: View) : Binding(view) {
    val chipView: Chip by bindView(R.id.chipView)
}