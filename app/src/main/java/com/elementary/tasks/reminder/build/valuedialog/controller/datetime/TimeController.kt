package com.elementary.tasks.reminder.build.valuedialog.controller.datetime

import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.databinding.BuilderItemTimeBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController
import org.threeten.bp.LocalTime

class TimeController(
  builderItem: BuilderItem<LocalTime>,
  private val is24Format: Boolean
) : AbstractBindingValueController<LocalTime, BuilderItemTimeBinding>(builderItem) {

  override fun bindView(layoutInflater: LayoutInflater, parent: ViewGroup): BuilderItemTimeBinding {
    return BuilderItemTimeBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.timePickerView.setIs24HourView(is24Format)
    binding.timePickerView.setOnTimeChangedListener { _, hourOfDay, minute ->
      updateValue(LocalTime.of(hourOfDay, minute))
    }
  }

  override fun onDataChanged(data: LocalTime?) {
    super.onDataChanged(data)
    data?.also {
      binding.timePickerView.hour = it.hour
      binding.timePickerView.minute = it.minute
    }
  }
}
