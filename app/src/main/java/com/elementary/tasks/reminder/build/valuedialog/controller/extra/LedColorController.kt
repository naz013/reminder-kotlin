package com.elementary.tasks.reminder.build.valuedialog.controller.extra

import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.databinding.BuilderItemLedColorBinding
import com.elementary.tasks.reminder.build.LedColorBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class LedColorController(
  builderItem: LedColorBuilderItem
) : AbstractBindingValueController<Int, BuilderItemLedColorBinding>(builderItem) {

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemLedColorBinding {
    return BuilderItemLedColorBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.ledGroup.setOnCheckedChangeListener { _, checkedId ->
      updateState(ledFromChip(checkedId))
    }
  }

  override fun onDataChanged(data: Int?) {
    super.onDataChanged(data)
    data?.also {
      binding.ledGroup.check(chipIdFromLed(it))
    } ?: run {
      binding.ledGroup.check(R.id.ledBlue)
    }
  }

  private fun updateState(led: Int) {
    updateValue(led)
  }

  private fun chipIdFromLed(id: Int): Int {
    return when (id) {
      0 -> R.id.ledRed
      1 -> R.id.ledGreen
      2 -> R.id.ledBlue
      3 -> R.id.ledYellow
      4 -> R.id.ledPink
      5 -> R.id.ledOrange
      6 -> R.id.ledTeal
      else -> R.id.ledBlue
    }
  }

  private fun ledFromChip(id: Int): Int {
    return when (id) {
      R.id.ledRed -> 0
      R.id.ledGreen -> 1
      R.id.ledBlue -> 2
      R.id.ledYellow -> 3
      R.id.ledPink -> 4
      R.id.ledOrange -> 5
      R.id.ledTeal -> 6
      else -> 2
    }
  }
}
