package com.elementary.tasks.reminder.build.valuedialog.controller.extra

import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.core.views.common.ValueSliderView
import com.elementary.tasks.databinding.BuilderItemRepeatLimitBinding
import com.elementary.tasks.reminder.build.RepeatLimitBuilderItem
import com.elementary.tasks.reminder.build.formatter.RepeatLimitFormatter
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class RepeatLimitController(
  builderItem: RepeatLimitBuilderItem,
  private val formatter: RepeatLimitFormatter = builderItem.repeatLimitFormatter
) : AbstractBindingValueController<Int, BuilderItemRepeatLimitBinding>(builderItem) {

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemRepeatLimitBinding {
    return BuilderItemRepeatLimitBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.valueSliderView.valueFormatter = object : ValueSliderView.ValueFormatter {
      override fun apply(value: Float): String {
        return formatter.format(value.toInt())
      }
    }
    binding.valueSliderView.setRange(-1f, 365f, 1f)
    binding.valueSliderView.onValueChangeListener = object : ValueSliderView.OnValueChangeListener {
      override fun onChanged(value: Float, displayValue: String) {
        updateValue(value.toInt())
      }
    }
  }

  override fun onDataChanged(data: Int?) {
    super.onDataChanged(data)
    data?.also {
      binding.valueSliderView.value = it.toFloat()
    } ?: run {
      binding.valueSliderView.value = 0f
    }
  }
}
