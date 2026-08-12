package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.naz013.domain.PublicHoliday
import org.threeten.bp.LocalDate

@Entity(
  tableName = "Holiday",
  indices = [
    Index(value = ["countryCode", "date"]),
    Index(value = ["countryCode", "year"])
  ]
)
internal data class HolidayEntity(
  @PrimaryKey
  val id: String,
  val countryCode: String,
  val year: Int,
  val date: Long,
  val name: String,
  val nameLocal: String?,
  val type: String,
  val location: String?,
) {

  constructor(holiday: PublicHoliday) : this(
    id = holiday.id,
    countryCode = holiday.countryCode,
    year = holiday.date.year,
    date = holiday.date.toEpochDay(),
    name = holiday.name,
    nameLocal = holiday.nameLocal,
    type = holiday.type,
    location = holiday.location
  )

  fun toDomain(): PublicHoliday {
    return PublicHoliday(
      id = id,
      countryCode = countryCode,
      date = LocalDate.ofEpochDay(date),
      name = name,
      nameLocal = nameLocal,
      type = type,
      location = location
    )
  }
}
