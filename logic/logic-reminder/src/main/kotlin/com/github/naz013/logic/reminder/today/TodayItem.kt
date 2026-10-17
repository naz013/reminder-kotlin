package com.github.naz013.logic.reminder.today

import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

enum class TodayBucket {
  Timed,
  AllDay,
  Anytime
}

sealed class TodayItem {
  abstract val id: String
  abstract val bucket: TodayBucket

  /** Local wall-clock moment used for ordering inside the bucket. */
  abstract val dateTime: LocalDateTime

  data class Reminder(
    val reminder: ReminderV2,
    override val dateTime: LocalDateTime,
    val isOverdue: Boolean,
    val alsoInCalendar: Boolean = false,
    val alsoInTasks: Boolean = false,
  ) : TodayItem() {
    override val id: String get() = reminder.uuId
    override val bucket: TodayBucket get() = TodayBucket.Timed
  }

  data class Birthday(
    val birthday: com.github.naz013.domain.Birthday,
    override val dateTime: LocalDateTime,
    override val bucket: TodayBucket,
  ) : TodayItem() {
    override val id: String get() = birthday.uuId
  }

  data class CalendarEvent(
    val event: GoogleCalendarEvent,
    override val dateTime: LocalDateTime,
    val endDateTime: LocalDateTime?,
  ) : TodayItem() {
    override val id: String get() = "${event.uuId}_$dateTime"
    override val bucket: TodayBucket
      get() = if (event.allDay) TodayBucket.AllDay else TodayBucket.Timed
  }

  data class Task(
    val task: GoogleTask,
    override val dateTime: LocalDateTime,
    override val bucket: TodayBucket,
  ) : TodayItem() {
    override val id: String get() = task.taskId
  }
}
