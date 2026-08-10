package com.github.naz013.holidays.remote

import com.google.firebase.firestore.PropertyName

/**
 * Wire shape of `holidays/{countryCode}/{year}/data`, written by the Python upload pipeline
 * (see research/PUBLIC_HOLIDAY_INTEGRATION.md and PUBLIC_HOLIDAY_INTEGRATION_PLAN.md).
 *
 * Deserialized via [com.google.firebase.firestore.DocumentSnapshot.toObject], which is the
 * Firestore SDK's own POJO mapper - it does NOT read Gson's `@SerializedName` (that annotation is
 * silently ignored here), it needs Firestore's own [PropertyName] to remap a snake_case document
 * field to a differently-named Kotlin property.
 */
internal data class HolidayDocumentDto(
  @PropertyName("country_code")
  val countryCode: String? = null,
  @PropertyName("country_name")
  val countryName: String? = null,
  val year: Int? = null,
  @PropertyName("holiday_count")
  val holidayCount: Int? = null,
  val holidays: List<HolidayItemDto>? = null,
  @PropertyName("last_updated")
  val lastUpdated: String? = null,
)

internal data class HolidayItemDto(
  val name: String? = null,
  @PropertyName("name_local")
  val nameLocal: String? = null,
  val date: String? = null,
  val type: String? = null,
  val location: String? = null,
)
