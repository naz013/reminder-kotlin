package com.elementary.tasks.core.utils.datetime

import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceDateTimeTag
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceManager
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import org.threeten.bp.LocalDateTime

class RecurEventManager(
  private val recurrenceManager: RecurrenceManager
) {

  fun getNextAfterDateTime(dateTime: LocalDateTime?, recurObject: String?): LocalDateTime? {
    if (recurObject == null || dateTime == null) return null

    val map = recurrenceManager.parseObject(recurObject) ?: return null

    val recurrenceDateTimeTag = map.getTagOrNull<RecurrenceDateTimeTag>(TagType.RDATE) ?: return null
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
