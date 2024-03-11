package com.elementary.tasks.reminder.build.valuedialog.controller.core

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.core.views.common.ValueAndTypePickerView
import com.elementary.tasks.databinding.BuilderItemValueAndTypeBinding
import com.elementary.tasks.reminder.build.BuilderItem

abstract class AbstractValueAndTypeController<T>(
  builderItem: BuilderItem<T>
) : AbstractBindingValueController<T, BuilderItemValueAndTypeBinding>(builderItem) {

  protected abstract fun convertToValue(textValue: String, typeIndex: Int): T?
  protected abstract fun parseValueAndType(t: T?): ValueAndType
  protected abstract fun getSelectionItems(): List<String>

  override fun isDraggable(): Boolean {
    return false
  }

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemValueAndTypeBinding {
    return BuilderItemValueAndTypeBinding.inflate(layoutInflater, parent, false)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onViewCreated() {
    super.onViewCreated()
    binding.valueAndTypePickerView.setOnTouchListener { v, event ->
      v.parent.requestDisallowInterceptTouchEvent(true)
      v.onTouchEvent(event)
      true
    }

    binding.valueAndTypePickerView.onChangedListener =
      object : ValueAndTypePickerView.OnChangedListener {
        override fun onChanged(value: String, typeIndex: Int) {
          updateValue(convertToValue(value, typeIndex))
        }
      }
    binding.valueAndTypePickerView.setItems(getSelectionItems())
  }

  override fun onDataChanged(data: T?) {
    super.onDataChanged(data)
    val valueAndType = parseValueAndType(data)
    binding.valueAndTypePickerView.setValue(valueAndType.value)
    binding.valueAndTypePickerView.setTypeSelection(valueAndType.typeIndex)
  }

  protected data class ValueAndType(
    val value: String,
    val typeIndex: Int
  )
}
