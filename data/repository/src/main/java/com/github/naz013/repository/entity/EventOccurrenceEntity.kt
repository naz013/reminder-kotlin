package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.domain.occurance.OccurrenceType
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

@Entity(tableName = "EventOccurrence")
internal data class EventOccurrenceEntity(
  @SerializedName("id")
  @PrimaryKey
  val id: String = "",
  @SerializedName("eventId")
  val eventId: String = "",
  @SerializedName("date")
  val date: Long = 0,
  @SerializedName("time")
  val time: Long = 0,
  @SerializedName("type")
  val type: String = "",
) {

  constructor(occurrence: EventOccurrence) : this(
    id = occurrence.id,
    eventId = occurrence.eventId,
    date = occurrence.date.toEpochDay(),
    time = occurrence.time.toSecondOfDay().toLong(),
    type = occurrence.type.name
  )

  fun toDomain(): EventOccurrence {
    return EventOccurrence(
      id = id,
      eventId = eventId,
      date = LocalDate.ofEpochDay(date),
      time = LocalTime.ofSecondOfDay(time),
      type = OccurrenceType.valueOf(type)
    )
  }
}
