package com.elementary.tasks.reminder.build.valuedialog.controller.extra

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IdRes
import com.elementary.tasks.R
import com.elementary.tasks.databinding.BuilderItemWindowTypeBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.formatter.WindowTypeFormatter
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController
import com.google.android.material.chip.Chip

class WindowTypeController(
  builderItem: BuilderItem<Int>
) : AbstractBindingValueController<Int, BuilderItemWindowTypeBinding>(builderItem) {

  private var mLastIdRes: Int = R.id.chip_fullscreen

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemWindowTypeBinding {
    return BuilderItemWindowTypeBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
      if (isAnyChecked()) {
        updateState(typeFromChip(checkedIds.first()))
      } else {
        chipView(mLastIdRes).isChecked = true
        updateState(typeFromChip(mLastIdRes))
      }
    }
  }

  override fun onDataChanged(data: Int?) {
    super.onDataChanged(data)
    data?.also {
      binding.chipGroup.check(chipIdFromType(it))
    }
  }

  private fun chipView(@IdRes id: Int): Chip {
    return when (id) {
      R.id.chip_fullscreen -> binding.chipFullscreen
      R.id.chip_simple -> binding.chipSimple
      else -> binding.chipFullscreen
    }
  }

  private fun isAnyChecked(): Boolean {
    return binding.chipFullscreen.isChecked || binding.chipSimple.isChecked
  }

  private fun chipIdFromType(id: Int): Int {
    return when (id) {
      WindowTypeFormatter.FULL_SCREEN -> R.id.chip_fullscreen
      WindowTypeFormatter.NOTIFICATION -> R.id.chip_simple
      else -> R.id.chip_fullscreen
    }
  }

  private fun typeFromChip(id: Int): Int {
    mLastIdRes = id
    return when (id) {
      R.id.chip_fullscreen -> WindowTypeFormatter.FULL_SCREEN
      R.id.chip_simple -> WindowTypeFormatter.NOTIFICATION
      else -> WindowTypeFormatter.FULL_SCREEN
    }
  }

  private fun updateState(type: Int) {
    updateValue(type)
  }
}
