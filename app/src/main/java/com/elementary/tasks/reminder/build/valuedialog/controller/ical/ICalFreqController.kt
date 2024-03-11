package com.elementary.tasks.reminder.build.valuedialog.controller.ical

import com.elementary.tasks.R
import com.elementary.tasks.core.os.startActivity
import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractTypeController
import com.elementary.tasks.reminder.create.fragments.recur.RecurHelpActivity
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter

class ICalFreqController(
  builderItem: BuilderItem<FreqType>,
  private val paramToTextAdapter: ParamToTextAdapter
) : AbstractTypeController<FreqType>(builderItem) {

  override fun convertToValue(typeIndex: Int): FreqType? {
    return FreqType.entries[typeIndex]
  }

  override fun getIndex(t: FreqType?): Int {
    return t?.let { FreqType.entries.indexOf(it) } ?: 0
  }

  override fun getSelectionItems(): List<String> {
    return FreqType.entries.map { paramToTextAdapter.getFreqText(it) }
  }

  override fun onViewCreated() {
    super.onViewCreated()
    addOptionalButton(R.drawable.ic_builder_ical_help)
  }

  override fun onOptionalClicked() {
    getContext().startActivity(RecurHelpActivity::class.java)
  }
}
