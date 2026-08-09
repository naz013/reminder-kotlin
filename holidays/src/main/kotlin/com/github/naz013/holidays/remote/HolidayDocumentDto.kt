package com.github.naz013.holidays.remote

import com.google.gson.annotations.SerializedName

/**
 * Wire shape of `holidays/{countryCode}/{year}/data`, written by the Python upload pipeline
 * (see research/PUBLIC_HOLIDAY_INTEGRATION.md and PUBLIC_HOLIDAY_INTEGRATION_PLAN.md).
 */
internal data class HolidayDocumentDto(
  @SerializedName("country_code")
  val countryCode: String? = null,
  @SerializedName("country_name")
  val countryName: String? = null,
  @SerializedName("year")
  val year: Int? = null,
  @SerializedName("holiday_count")
  val holidayCount: Int? = null,
  @SerializedName("holidays")
  val holidays: List<HolidayItemDto>? = null,
  @SerializedName("last_updated")
  val lastUpdated: String? = null,
)

internal data class HolidayItemDto(
  @SerializedName("name")
  val name: String? = null,
  @SerializedName("name_local")
  val nameLocal: String? = null,
  @SerializedName("date")
  val date: String? = null,
  @SerializedName("type")
  val type: String? = null,
  @SerializedName("location")
  val location: String? = null,
)
