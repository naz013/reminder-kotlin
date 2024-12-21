package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.naz013.domain.Birthday
import com.google.gson.annotations.SerializedName
import java.util.Random
import java.util.UUID

@Entity(tableName = "Birthday")
internal data class BirthdayEntity(
  @SerializedName("name")
  val name: String = "",
  @SerializedName("date")
  val date: String = "",
  @SerializedName("number")
  val number: String = "",
  @SerializedName("key")
  val key: String = "",
  @SerializedName("showedYear")
  val showedYear: Int = 0,
  @SerializedName("contactId")
  val contactId: Long = 0L,
  @SerializedName("day")
  val day: Int = 0,
  @SerializedName("month")
  val month: Int = 0,
  @SerializedName("uniqueId")
  val uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
  @SerializedName("dayMonth")
  val dayMonth: String = "",
  @SerializedName("uuId")
  @PrimaryKey
  val uuId: String = UUID.randomUUID().toString(),
  @SerializedName("updatedAt")
  val updatedAt: String? = null,
  @SerializedName("ignoreYear")
  val ignoreYear: Boolean = false
) {

  constructor(birthday: Birthday) : this(
    name = birthday.name,
    date = birthday.date,
    number = birthday.number,
    key = birthday.key,
    showedYear = birthday.showedYear,
    contactId = birthday.contactId,
    day = birthday.day,
    month = birthday.month,
    uniqueId = birthday.uniqueId,
    dayMonth = birthday.dayMonth,
    uuId = birthday.uuId,
    updatedAt = birthday.updatedAt,
    ignoreYear = birthday.ignoreYear
  )

  fun toDomain(): Birthday {
    return Birthday(
      name = name,
      date = date,
      number = number,
      key = key,
      showedYear = showedYear,
      contactId = contactId,
      day = day,
      month = month,
      uniqueId = uniqueId,
      dayMonth = dayMonth,
      uuId = uuId,
      updatedAt = updatedAt,
      ignoreYear = ignoreYear
    )
  }
}
