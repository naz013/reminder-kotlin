package com.elementary.tasks.reminder.build.valuedialog.controller.extra

import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.core.views.common.VerticalWheelSelector
import com.elementary.tasks.databinding.BuilderItemPriorityBinding
import com.elementary.tasks.reminder.build.PriorityBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class PriorityController(
  builderItem: PriorityBuilderItem
) : AbstractBindingValueController<Int, BuilderItemPriorityBinding>(builderItem) {

  override fun isDraggable(): Boolean {
    return false
  }

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemPriorityBinding {
    return BuilderItemPriorityBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.prioritySelectorView.onSelectionChangedListener =
      object : VerticalWheelSelector.OnSelectionChangedListener {
        override fun onSelectionChanged(position: Int, selectedItem: String) {
          updateValue(position)
        }
      }
  }

  override fun onDataChanged(data: Int?) {
    super.onDataChanged(data)
    data?.also {
      binding.prioritySelectorView.selectItem(it)
    } ?: run {
      binding.prioritySelectorView.selectItem(2)
    }
  }
}
