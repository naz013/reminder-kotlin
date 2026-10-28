package com.github.naz013.logic.reminder

import com.github.naz013.domain.reminder.v2.toKnownTimeZoneIdOrNull
import com.github.naz013.icalendar.DateTimeStartTag
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.RecurrenceDateTimeTag
import com.github.naz013.icalendar.TagType
import com.github.naz013.icalendar.UtcDateTime
import com.github.naz013.logging.Logger
import kotlin.coroutines.cancellation.CancellationException
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class RecurEventManager(
  private val iCalendarApi: ICalendarApi,
) {
  /**
   * @param dateTime wall-clock time in [zone]
   * @param zone the reminder's fixed zone, or null for the device zone (floating)
   * @return the next occurrence as wall-clock time in [zone]
   */
  fun getNextAfterDateTime(
    dateTime: LocalDateTime?,
    recurObject: String?,
    zone: ZoneId? = null,
  ): LocalDateTime? {
    if (recurObject == null || dateTime == null) return null

    val map = iCalendarApi.parseObject(recurObject) ?: return null

    val recurrenceDateTimeTag =
      map.getTagOrNull<RecurrenceDateTimeTag>(TagType.RDATE)
        ?: return null
    val list = recurrenceDateTimeTag.values
    val callerZone = zone ?: ZoneId.systemDefault()
    val listZone = recurrenceDateTimeTag.zoneOrDevice()

    val index = findIndex(dateTime.withNano(0).convert(callerZone, listZone), list)

    return if (index != -1 && index < list.size - 1) {
      list[index + 1].dateTime?.convert(listZone, callerZone)
    } else {
      null
    }
  }

  /**
   * Rebuilds the precomputed RDATE list of [recurObject] in [zone] (null = device zone) keeping the DTSTART
   * wall-clock. Idempotent. Returns null when the object cannot be rebuilt, so callers keep the old value.
   */
  fun regenerate(
    recurObject: String?,
    zone: ZoneId?,
  ): String? {
    if (recurObject.isNullOrEmpty()) return null
    return try {
      val map = iCalendarApi.parseObject(recurObject) ?: return null
      val start = map.getTagOrNull<DateTimeStartTag>(TagType.DTSTART) ?: return null
      val updated = map.copy(map = map.map + (TagType.DTSTART to start.copy(timeZoneId = zone?.id)))
      iCalendarApi.createObject(updated)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to regenerate recur object", e)
      null
    }
  }

  private fun RecurrenceDateTimeTag.zoneOrDevice(): ZoneId =
    timeZoneId
      .toKnownTimeZoneIdOrNull()
      ?.let { runCatching { ZoneId.of(it) }.getOrNull() }
      ?: ZoneId.systemDefault()

  private fun LocalDateTime.convert(
    from: ZoneId,
    to: ZoneId,
  ): LocalDateTime =
    if (from == to) this else ZonedDateTime.of(this, from).withZoneSameInstant(to).toLocalDateTime()

  private fun findIndex(
    dateTime: LocalDateTime,
    list: List<UtcDateTime>,
  ): Int = list.indexOfFirst { it.dateTime == dateTime }

  companion object {
    private const val TAG = "RecurEventManager"
  }
}
