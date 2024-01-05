package com.elementary.tasks.reminder.build.valuedialog.controller.datetime

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.core.views.common.NumberValuePickerView
import com.elementary.tasks.databinding.BuilderItemRepeatIntervalBinding
import com.elementary.tasks.reminder.build.RepeatIntervalBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class RepeatIntervalController(
  builderItem: RepeatIntervalBuilderItem
) : AbstractBindingValueController<Long, BuilderItemRepeatIntervalBinding>(builderItem) {

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemRepeatIntervalBinding {
    return BuilderItemRepeatIntervalBinding.inflate(layoutInflater, parent, false)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onViewCreated() {
    super.onViewCreated()
    binding.numberPickerView.onValueChangedListener =
      object : NumberValuePickerView.OnValueChangedListener {
        override fun onChanged(value: String) {
          updateValue(runCatching { value.toLong() }.getOrNull())
        }
      }
  }

  override fun onDataChanged(data: Long?) {
    super.onDataChanged(data)
    data?.also { binding.numberPickerView.setValue(it.toString()) }
  }
}
