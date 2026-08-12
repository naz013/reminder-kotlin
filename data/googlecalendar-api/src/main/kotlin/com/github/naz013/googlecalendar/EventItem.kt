package com.github.naz013.googlecalendar

data class EventItem(
  val title: String,
  val description: String,
  val rrule: String,
  private val rDate: String,
  val calendarId: Long,
  val allDay: Int,
  val dtStart: Long,
  val dtEnd: Long,
  val id: Long,
  var localId: String = "",
  var calendarName: String = "",
)
