package com.elementary.tasks.reminder.build.valuedialog.controller.datetime

import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.databinding.BuilderItemDateBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController
import org.threeten.bp.LocalDate

class DateController(
  builderItem: BuilderItem<LocalDate>
) : AbstractBindingValueController<LocalDate, BuilderItemDateBinding>(builderItem) {

  override fun bindView(layoutInflater: LayoutInflater, parent: ViewGroup): BuilderItemDateBinding {
    return BuilderItemDateBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.datePickerView.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
      updateValue(LocalDate.of(year, monthOfYear + 1, dayOfMonth))
    }
  }

  override fun onDataChanged(data: LocalDate?) {
    super.onDataChanged(data)
    data?.also {
      binding.datePickerView.updateDate(it.year, it.monthValue - 1, it.dayOfMonth)
    }
  }
}
