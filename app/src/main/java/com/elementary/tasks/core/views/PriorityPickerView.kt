package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import com.elementary.tasks.R
import com.elementary.tasks.databinding.ViewPriorityBinding
import com.google.android.material.chip.Chip

class PriorityPickerView : LinearLayout {

  private lateinit var binding: ViewPriorityBinding
  var onPriorityChaneListener: ((Int) -> Unit)? = null
  var priority: Int = 2
    set(value) {
      field = value
      binding.chipGroup.check(chipIdFromPriority(value))
    }
    get() {
      return priorityFromChip(binding.chipGroup.checkedChipId)
    }
  private var mLastIdRes: Int = R.id.chipNormal

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    init(context)
  }

  private fun chipIdFromPriority(id: Int): Int {
    return when (id) {
      0 -> R.id.chipLowest
      1 -> R.id.chipLow
      2 -> R.id.chipNormal
      3 -> R.id.chipHigh
      4 -> R.id.chipHighest
      else -> R.id.chipNormal
    }
  }

  private fun priorityFromChip(id: Int): Int {
    mLastIdRes = id
    return when (id) {
      R.id.chipLowest -> 0
      R.id.chipLow -> 1
      R.id.chipNormal -> 2
      R.id.chipHigh -> 3
      R.id.chipHighest -> 4
      else -> 2
    }
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_priority, this)
    orientation = VERTICAL
    binding = ViewPriorityBinding.bind(this)

    binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
      if (isAnyChecked()) {
        updateState(priorityFromChip(checkedIds.first()))
      } else {
        chipView(mLastIdRes).isChecked = true
        updateState(priorityFromChip(mLastIdRes))
      }
    }
  }

  private fun chipView(@IdRes id: Int): Chip {
    return when (id) {
      R.id.chipLowest -> binding.chipLowest
      R.id.chipLow -> binding.chipLow
      R.id.chipNormal -> binding.chipNormal
      R.id.chipHigh -> binding.chipHigh
      R.id.chipHighest -> binding.chipHighest
      else -> binding.chipNormal
    }
  }

  private fun isAnyChecked(): Boolean {
    return binding.chipLowest.isChecked || binding.chipLow.isChecked || binding.chipNormal.isChecked
      || binding.chipHigh.isChecked || binding.chipHighest.isChecked
  }

  private fun updateState(priority: Int) {
    onPriorityChaneListener?.invoke(priority)
  }
}
