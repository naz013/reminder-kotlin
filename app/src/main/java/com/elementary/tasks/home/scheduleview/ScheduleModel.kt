package com.elementary.tasks.home.scheduleview

import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.home.scheduleview.data.UiBirthdayScheduleList
import com.elementary.tasks.home.scheduleview.data.UiReminderScheduleList
import org.threeten.bp.LocalDateTime

sealed class ScheduleModel {
  abstract val id: String
  abstract val dateTime: LocalDateTime?
  abstract val viewType: ScheduleModelViewType
}

data class ReminderScheduleModel(
  val data: UiReminderScheduleList
) : ScheduleModel() {
  override val id: String = data.id
  override val dateTime: LocalDateTime? = data.dueDateTime
  override val viewType: ScheduleModelViewType = ScheduleModelViewType.REMINDER
}

data class BirthdayScheduleModel(
  val data: UiBirthdayScheduleList
) : ScheduleModel() {
  override val id: String = data.id
  override val dateTime: LocalDateTime = data.dueDateTime
  override val viewType: ScheduleModelViewType = ScheduleModelViewType.BIRTHDAY
}

data class HeaderScheduleModel(
  val text: String,
  val headerTimeType: HeaderTimeType
) : ScheduleModel() {
  override val id: String = text
  override val dateTime: LocalDateTime? = null
  override val viewType: ScheduleModelViewType = ScheduleModelViewType.HEADER
}

data class ReminderAndNoteScheduleModel(
  val reminder: UiReminderScheduleList,
  val note: UiNoteList
) : ScheduleModel() {
  override val id: String = reminder.id
  override val dateTime: LocalDateTime? = reminder.dueDateTime
  override val viewType: ScheduleModelViewType = ScheduleModelViewType.REMINDER_NOTE
}

data class ReminderAndGoogleTaskScheduleModel(
  val reminder: UiReminderScheduleList,
  val googleTask: UiGoogleTaskList
) : ScheduleModel() {
  override val id: String = reminder.id
  override val dateTime: LocalDateTime? = reminder.dueDateTime
  override val viewType: ScheduleModelViewType = ScheduleModelViewType.REMINDER_GTASK
}

enum class ScheduleModelViewType(val value: Int) {
  REMINDER(0),
  BIRTHDAY(2),
  HEADER(3),
  REMINDER_NOTE(4),
  REMINDER_GTASK(6);

  companion object {
    fun fromValue(value: Int): ScheduleModelViewType {
      return entries.first { it.value == value }
    }
  }
}

enum class HeaderTimeType {
  MORNING,
  NOON,
  EVENING
}
