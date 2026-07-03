package com.elementary.tasks.reminder.build.valuedialog.controller.ical

import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.views.common.ValueSliderView
import com.elementary.tasks.databinding.BuilderItemRepeatLimitBinding
import com.elementary.tasks.reminder.build.ICalIntBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController
import com.elementary.tasks.reminder.recur.RecurHelpActivity
import com.github.naz013.ui.common.context.startActivity

class ICalIntController(
  builderItem: ICalIntBuilderItem,
) : AbstractBindingValueController<Int, BuilderItemRepeatLimitBinding>(builderItem) {
  private val formatter = builderItem.formatter
  private val minValue = builderItem.minValue.toFloat()
  private val maxValue = builderItem.maxValue.toFloat()

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup,
  ): BuilderItemRepeatLimitBinding = BuilderItemRepeatLimitBinding.inflate(layoutInflater, parent, false)

  override fun onViewCreated() {
    super.onViewCreated()
    addOptionalButton(R.drawable.ic_builder_ical_help)
    binding.valueSliderView.valueFormatter =
      object : ValueSliderView.ValueFormatter {
        override fun apply(value: Float): String = formatter.format(value.toInt())
      }
    binding.valueSliderView.setRange(minValue, maxValue, 1f)
    binding.valueSliderView.onValueChangeListener =
      object : ValueSliderView.OnValueChangeListener {
        override fun onChanged(
          value: Float,
          displayValue: String,
        ) {
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

  override fun onOptionalClicked() {
    getContext().startActivity(RecurHelpActivity::class.java)
  }
}
