package com.github.naz013.holidays.remote

import com.github.naz013.domain.PublicHoliday
import org.threeten.bp.LocalDate

/**
 * Maps one raw holiday entry from Firestore's `holidays` array field to [PublicHoliday].
 *
 * Deliberately does not go through [com.google.firebase.firestore.DocumentSnapshot.toObject] /
 * a `@PropertyName`-annotated DTO: Firestore's Kotlin data-class reflection resolves that
 * annotation's use-site target inconsistently across SDK versions, which previously left
 * `name_local` unpopulated even when annotated correctly. Firestore already hands back nested
 * document fields as a plain `Map<String, Any?>` (arrays as `List<Any?>`), so reading the raw map
 * by literal key name here is both simpler and fully deterministic - no reflection involved.
 */
internal fun Map<String, Any?>.toPublicHolidayOrNull(countryCode: String): PublicHoliday? {
  val holidayName = (this[FIELD_NAME] as? String)?.takeIf { it.isNotBlank() }
  val parsedDate = (this[FIELD_DATE] as? String)
    ?.let { dateString -> runCatching { LocalDate.parse(dateString) }.getOrNull() }

  if (holidayName == null || parsedDate == null) return null

  return PublicHoliday(
    id = PublicHoliday.id(countryCode, parsedDate, holidayName),
    countryCode = countryCode,
    date = parsedDate,
    name = holidayName,
    nameLocal = this[FIELD_NAME_LOCAL] as? String,
    type = (this[FIELD_TYPE] as? String).orEmpty(),
    location = this[FIELD_LOCATION] as? String,
  )
}

private const val FIELD_NAME = "name"
private const val FIELD_NAME_LOCAL = "name_local"
private const val FIELD_DATE = "date"
private const val FIELD_TYPE = "type"
private const val FIELD_LOCATION = "location"
