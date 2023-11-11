package com.elementary.tasks.home.scheduleview

import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.data.ui.note.UiNoteList
import org.threeten.bp.LocalDateTime

sealed class ScheduleModel {
  abstract val id: String
  abstract val dateTime: LocalDateTime?
  abstract val viewType: ScheduleModelViewType
}

data class ReminderScheduleModel(
  val data: UiReminderListData
) : ScheduleModel() {
  override val id: String = data.id
  override val dateTime: LocalDateTime? = data.due?.localDateTime
  override val viewType: ScheduleModelViewType = if (data is UiReminderListActiveShop) {
    ScheduleModelViewType.REMINDER_SHOPPING
  } else {
    ScheduleModelViewType.REMINDER
  }
}

data class BirthdayScheduleModel(
  val data: UiBirthdayList
) : ScheduleModel() {
  override val id: String = data.uuId
  override val dateTime: LocalDateTime = data.nextBirthdayDate
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
  val reminder: UiReminderListData,
  val note: UiNoteList
) : ScheduleModel() {
  override val id: String = reminder.id
  override val dateTime: LocalDateTime? = reminder.due?.localDateTime
  override val viewType: ScheduleModelViewType = if (reminder is UiReminderListActiveShop) {
    ScheduleModelViewType.REMINDER_SHOPPING_NOTE
  } else {
    ScheduleModelViewType.REMINDER_NOTE
  }
}

data class ReminderAndGoogleTaskScheduleModel(
  val reminder: UiReminderListData,
  val googleTask: UiGoogleTaskList
) : ScheduleModel() {
  override val id: String = reminder.id
  override val dateTime: LocalDateTime? = reminder.due?.localDateTime
  override val viewType: ScheduleModelViewType = if (reminder is UiReminderListActiveShop) {
    ScheduleModelViewType.REMINDER_SHOPPING_GTASK
  } else {
    ScheduleModelViewType.REMINDER_GTASK
  }
}

enum class ScheduleModelViewType(val value: Int) {
  REMINDER(0),
  REMINDER_SHOPPING(1),
  BIRTHDAY(2),
  HEADER(3),
  REMINDER_NOTE(4),
  REMINDER_SHOPPING_NOTE(5),
  REMINDER_GTASK(6),
  REMINDER_SHOPPING_GTASK(7);

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
