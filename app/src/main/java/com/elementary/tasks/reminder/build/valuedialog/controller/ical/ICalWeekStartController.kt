package com.elementary.tasks.reminder.build.valuedialog.controller.ical

import com.elementary.tasks.R
import com.elementary.tasks.core.os.startActivity
import com.elementary.tasks.core.utils.datetime.recurrence.Day
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractTypeController
import com.elementary.tasks.reminder.create.fragments.recur.RecurHelpActivity
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter

class ICalWeekStartController(
  builderItem: BuilderItem<DayValue>,
  private val paramToTextAdapter: ParamToTextAdapter
) : AbstractTypeController<DayValue>(builderItem) {

  override fun convertToValue(typeIndex: Int): DayValue? {
    val entries = Day.entries
    if (typeIndex < 0 || typeIndex >= entries.size) {
      return null
    }
    return DayValue(entries[typeIndex])
  }

  override fun getIndex(t: DayValue?): Int {
    return t?.day?.let { Day.entries.indexOf(it) } ?: 0
  }

  override fun getSelectionItems(): List<String> {
    return Day.entries.map { DayValue(it) }.map { paramToTextAdapter.getDayFullText(it) }
  }

  override fun onViewCreated() {
    super.onViewCreated()
    addOptionalButton(R.drawable.ic_builder_ical_help)
  }

  override fun onOptionalClicked() {
    getContext().startActivity(RecurHelpActivity::class.java)
  }
}
