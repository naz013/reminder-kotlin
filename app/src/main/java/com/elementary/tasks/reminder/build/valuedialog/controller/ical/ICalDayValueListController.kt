package com.elementary.tasks.reminder.build.valuedialog.controller.ical

import com.elementary.tasks.R
import com.github.naz013.feature.common.android.startActivity
import com.elementary.tasks.core.utils.datetime.recurrence.Day
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractSelectableArrayController
import com.elementary.tasks.reminder.create.fragments.recur.RecurHelpActivity
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter

class ICalDayValueListController(
  builderItem: BuilderItem<List<DayValue>>,
  private val paramToTextAdapter: ParamToTextAdapter
) :
  AbstractSelectableArrayController<List<DayValue>,
    AbstractSelectableArrayController.SimpleSelectableValue<DayValue>>(
    builderItem = builderItem,
    multiChoice = true,
    numOfColumns = 3
  ) {

  override fun getAdapterData(): List<SimpleSelectableValue<DayValue>> {
    val selected = builderItem.modifier.getValue()?.associate { it to it } ?: emptyMap()
    return Day.entries.map { DayValue(it) }
      .map {
        SimpleSelectableValue(
          value = it,
          uiValue = paramToTextAdapter.getDayFullText(it),
          selectionState = selected.containsKey(it)
        )
      }
  }

  override fun updateValue(selectedItems: List<SimpleSelectableValue<DayValue>>) {
    if (selectedItems.isEmpty()) {
      updateValue(null)
    } else {
      updateValue(selectedItems.map { it.value })
    }
  }

  override fun onViewCreated() {
    super.onViewCreated()
    addOptionalButton(R.drawable.ic_builder_ical_help)
  }

  override fun onOptionalClicked() {
    getContext().startActivity(RecurHelpActivity::class.java)
  }
}
