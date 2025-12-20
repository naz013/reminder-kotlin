package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.domain.history.EventHistoricalRecordType
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

@Entity(tableName = "EventHistory")
internal data class EventHistoryEntity(
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

  constructor(record: EventHistoricalRecord) : this(
    id = record.id,
    eventId = record.eventId,
    date = record.date.toEpochDay(),
    time = record.time.toSecondOfDay().toLong(),
    type = record.type.name
  )

  fun toDomain(): EventHistoricalRecord {
    return EventHistoricalRecord(
      id = id,
      eventId = eventId,
      date = LocalDate.ofEpochDay(date),
      time = LocalTime.ofSecondOfDay(time),
      type = EventHistoricalRecordType.valueOf(type)
    )
  }
}
