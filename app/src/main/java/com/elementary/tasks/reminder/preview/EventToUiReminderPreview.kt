package com.elementary.tasks.reminder.preview

import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.os.ColorProvider
import com.elementary.tasks.core.os.UnitsConverter
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.core.text.UiTextStyle
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.reminder.preview.data.UiCalendarEventList
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewData
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewGoogleCalendar
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewHeader
import com.github.naz013.feature.common.android.TextProvider

class EventToUiReminderPreview(
  private val textProvider: TextProvider,
  private val colorProvider: ColorProvider,
  private val unitsConverter: UnitsConverter,
  private val dateTimeManager: DateTimeManager
) {

  operator fun invoke(
    events: List<GoogleCalendarUtils.EventItem>,
    calendars: List<GoogleCalendarUtils.CalendarItem>
  ): List<UiReminderPreviewData> {
    val calendarsMap = calendars.associateBy { it.id }
    val list = events.map { item ->
      UiCalendarEventList(
        id = item.id,
        localId = item.localId,
        title = item.title,
        description = item.description,
        calendarName = calendarsMap[item.calendarId]?.name,
        dateStartFormatted = dateTimeManager.getFullDateTime(item.dtStart)
          .takeIf { item.dtStart != 0L },
        dateEndFormatted = dateTimeManager.getFullDateTime(item.dtEnd)
          .takeIf { item.dtEnd != 0L }
      )
    }.map { UiReminderPreviewGoogleCalendar(it) }

    return listOf(
      UiReminderPreviewHeader(
        UiTextElement(
          text = textProvider.getText(R.string.events),
          textFormat = UiTextFormat(
            fontSize = unitsConverter.spToPx(18f),
            textStyle = UiTextStyle.BOLD,
            textColor = colorProvider.getColorOnBackground()
          )
        )
      )
    ) + list
  }
}
