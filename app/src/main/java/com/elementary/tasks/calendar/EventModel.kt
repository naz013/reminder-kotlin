package com.elementary.tasks.calendar

import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList

sealed class EventModel {
  abstract val viewType: Int
  abstract val millis: Long
  abstract val day: Int
  abstract val monthValue: Int
  abstract val year: Int

  companion object {
    const val REMINDER = 0
    const val BIRTHDAY = 2
  }
}

data class ReminderEventModel(
  val model: UiReminderListData,
  override val day: Int,
  override val monthValue: Int,
  override val year: Int
) : EventModel() {
  override val viewType: Int = REMINDER
  override val millis: Long = model.due?.millis ?: 0L
}

data class BirthdayEventModel(
  val model: UiBirthdayList,
  override val day: Int,
  override val monthValue: Int,
  override val year: Int
) : EventModel() {
  override val viewType: Int = BIRTHDAY
  override val millis: Long = model.nextBirthdayDateMillis
}
