package com.elementary.tasks.core.utils.datetime

import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.RecurrenceDateTimeTag
import com.github.naz013.icalendar.TagType
import com.github.naz013.icalendar.UtcDateTime
import org.threeten.bp.LocalDateTime

class RecurEventManager(
  private val iCalendarApi: ICalendarApi
) {

  fun getNextAfterDateTime(dateTime: LocalDateTime?, recurObject: String?): LocalDateTime? {
    if (recurObject == null || dateTime == null) return null

    val map = iCalendarApi.parseObject(recurObject) ?: return null

    val recurrenceDateTimeTag = map.getTagOrNull<RecurrenceDateTimeTag>(TagType.RDATE)
      ?: return null
    val list = recurrenceDateTimeTag.values

    val index = findIndex(dateTime.withNano(0), list)

    return if (index != -1 && index < list.size - 1) {
      list[index + 1].dateTime
    } else {
      null
    }
  }

  private fun findIndex(dateTime: LocalDateTime, list: List<UtcDateTime>): Int {
    return list.indexOfFirst { it.dateTime == dateTime }
  }
}
