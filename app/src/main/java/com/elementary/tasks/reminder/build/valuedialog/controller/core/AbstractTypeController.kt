package com.elementary.tasks.reminder.build.valuedialog.controller.core

import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.core.views.common.VerticalWheelSelector
import com.elementary.tasks.databinding.BuilderItemVerticalSelectableBinding
import com.elementary.tasks.reminder.build.BuilderItem

abstract class AbstractTypeController<T>(
  builderItem: BuilderItem<T>
) : AbstractBindingValueController<T, BuilderItemVerticalSelectableBinding>(builderItem) {

  protected abstract fun convertToValue(typeIndex: Int): T?
  protected abstract fun getIndex(t: T?): Int
  protected abstract fun getSelectionItems(): List<String>

  override fun isDraggable(): Boolean {
    return false
  }

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemVerticalSelectableBinding {
    return BuilderItemVerticalSelectableBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.selectorView.onSelectionChangedListener =
      object : VerticalWheelSelector.OnSelectionChangedListener {
        override fun onSelectionChanged(position: Int, selectedItem: String) {
          updateValue(convertToValue(position))
        }
      }
    binding.selectorView.setItems(getSelectionItems())
  }

  override fun onDataChanged(data: T?) {
    super.onDataChanged(data)
    binding.selectorView.selectItem(getIndex(data))
  }
}
