package com.elementary.tasks.reminder.build.valuedialog.controller.google

import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.reminder.build.GoogleCalendarBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractSelectableRadioController
import com.elementary.tasks.reminder.build.valuedialog.controller.core.SelectableValue

class GoogleCalendarController(
  private val googleCalendarBuilderItem: GoogleCalendarBuilderItem,
  private val calendars: List<GoogleCalendarUtils.CalendarItem>
) :
  AbstractSelectableRadioController<GoogleCalendarUtils.CalendarItem,
    GoogleCalendarController.GoogleCalendarSelectableValue>(
    builderItem = googleCalendarBuilderItem
  ) {

  override fun clearValue() {
    builderItem.modifier.update(null)
  }

  override fun getAdapterData(): List<GoogleCalendarSelectableValue> {
    val value = builderItem.modifier.getValue()?.id
    return calendars.map {
      GoogleCalendarSelectableValue(it, it.id == value)
    }
  }

  override fun updateValue(selected: GoogleCalendarSelectableValue?) {
    updateValue(selected?.calendarItem)
  }

  data class GoogleCalendarSelectableValue(
    val calendarItem: GoogleCalendarUtils.CalendarItem,
    var selectionState: Boolean
  ) : SelectableValue {

    override fun getTitle(): String {
      return calendarItem.name
    }

    override fun isSelected(): Boolean {
      return selectionState
    }

    override fun setSelected(isSelected: Boolean) {
      this.selectionState = isSelected
    }
  }
}
