package com.github.naz013.domain

import org.threeten.bp.LocalDate

data class PublicHoliday(
  val id: String,
  val countryCode: String,
  val date: LocalDate,
  val name: String,
  val nameLocal: String?,
  val type: String,
  val location: String?,
) {
  companion object {
    fun id(countryCode: String, date: LocalDate, name: String): String = "$countryCode:$date:$name"
  }
}
