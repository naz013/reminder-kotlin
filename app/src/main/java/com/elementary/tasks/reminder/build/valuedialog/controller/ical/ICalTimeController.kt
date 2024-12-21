package com.elementary.tasks.reminder.build.valuedialog.controller.ical

import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.R
import com.github.naz013.feature.common.android.startActivity
import com.elementary.tasks.databinding.BuilderItemTimeBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController
import com.elementary.tasks.reminder.create.fragments.recur.RecurHelpActivity
import org.threeten.bp.LocalTime

class ICalTimeController(
  builderItem: BuilderItem<LocalTime>,
  private val is24Format: Boolean
) : AbstractBindingValueController<LocalTime, BuilderItemTimeBinding>(builderItem) {

  override fun bindView(layoutInflater: LayoutInflater, parent: ViewGroup): BuilderItemTimeBinding {
    return BuilderItemTimeBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    addOptionalButton(R.drawable.ic_builder_ical_help)
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

  override fun onOptionalClicked() {
    getContext().startActivity(RecurHelpActivity::class.java)
  }
}
