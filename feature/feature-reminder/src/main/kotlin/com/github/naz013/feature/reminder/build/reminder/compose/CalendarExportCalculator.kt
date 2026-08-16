package com.github.naz013.feature.reminder.build.reminder.compose

import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.GoogleCalendarBuilderItem
import com.github.naz013.feature.reminder.build.GoogleCalendarDurationBuilderItem
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.CalendarExportSettings

class CalendarExportCalculator {
  operator fun invoke(itemsMap: Map<BiType, BuilderItem<*>>): CalendarExportSettings? {
    val calendar = (itemsMap[BiType.GOOGLE_CALENDAR] as? GoogleCalendarBuilderItem)?.modifier?.getValue() ?: return null
    val duration = (itemsMap[BiType.GOOGLE_CALENDAR_DURATION] as? GoogleCalendarDurationBuilderItem)?.modifier?.getValue()

    return CalendarExportSettings(
      calendarId = calendar.id,
      duration = duration?.millis ?: 0L,
      allDay = duration?.allDay ?: false,
    )
  }
}
