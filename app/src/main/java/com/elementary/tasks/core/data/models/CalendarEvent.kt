package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class CalendarEvent(var reminderId: String, var event: String, var eventId: Long) {

  @PrimaryKey
  var uuId: String = UUID.randomUUID().toString()

  override fun toString(): String {
    return "CalendarEvent(reminderId='$reminderId', event='$event', eventId=$eventId, uuId='$uuId')"
  }
}
