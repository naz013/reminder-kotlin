package com.elementary.tasks.reminder.build.valuedialog.controller.ical

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import com.elementary.tasks.R
import com.elementary.tasks.core.os.startActivity
import com.elementary.tasks.databinding.BuilderItemDateBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController
import com.elementary.tasks.reminder.create.fragments.recur.RecurHelpActivity
import org.threeten.bp.LocalDate

class ICalDateController(
  builderItem: BuilderItem<LocalDate>
) : AbstractBindingValueController<LocalDate, BuilderItemDateBinding>(builderItem) {

  override fun bindView(layoutInflater: LayoutInflater, parent: ViewGroup): BuilderItemDateBinding {
    return BuilderItemDateBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    addOptionalButton(R.drawable.ic_builder_ical_help)
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

  override fun onOptionalClicked() {
    getContext().startActivity(RecurHelpActivity::class.java)
  }
}
