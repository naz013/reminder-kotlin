package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState

@Keep
internal data class ReminderWithGroupEntity(
  @Embedded val reminder: ReminderEntity,
  @Relation(
    parentColumn = "groupUuId",
    entityColumn = "groupUuId"
  )
  val reminderGroup: ReminderGroupEntity?
) {

  fun toDomain(): Reminder {
    return Reminder(
      summary = reminder.summary,
      noteId = reminder.noteId,
      reminderType = reminder.reminderType,
      eventState = reminder.eventState,
      groupUuId = reminder.groupUuId,
      uuId = reminder.uuId,
      eventTime = reminder.eventTime,
      startTime = reminder.startTime,
      eventCount = reminder.eventCount,
      color = reminder.color,
      delay = reminder.delay,
      vibrate = reminder.vibrate,
      repeatNotification = reminder.repeatNotification,
      notifyByVoice = reminder.notifyByVoice,
      awake = reminder.awake,
      unlock = reminder.unlock,
      exportToTasks = reminder.exportToTasks,
      exportToCalendar = reminder.exportToCalendar,
      useGlobal = reminder.useGlobal,
      from = reminder.from,
      to = reminder.to,
      hours = reminder.hours,
      fileName = reminder.fileName,
      melodyPath = reminder.melodyPath,
      volume = reminder.volume,
      dayOfMonth = reminder.dayOfMonth,
      monthOfYear = reminder.monthOfYear,
      repeatInterval = reminder.repeatInterval,
      repeatLimit = reminder.repeatLimit,
      after = reminder.after,
      weekdays = reminder.weekdays,
      type = reminder.type,
      target = reminder.target,
      subject = reminder.subject,
      attachmentFile = reminder.attachmentFile,
      attachmentFiles = reminder.attachmentFiles,
      auto = reminder.auto,
      places = reminder.places.map { it.toDomain() },
      shoppings = reminder.shoppings,
      uniqueId = reminder.uniqueId,
      isActive = reminder.isActive,
      isRemoved = reminder.isRemoved,
      isNotificationShown = reminder.isNotificationShown,
      isLocked = reminder.isLocked,
      hasReminder = reminder.hasReminder,
      duration = reminder.duration,
      calendarId = reminder.calendarId,
      remindBefore = reminder.remindBefore,
      windowType = reminder.windowType,
      priority = reminder.priority,
      updatedAt = reminder.updatedAt,
      taskListId = reminder.taskListId,
      recurDataObject = reminder.recurDataObject,
      allDay = reminder.allDay,
      description = reminder.description,
      builderScheme = reminder.builderScheme,
      jsonSchemaVersion = reminder.version,
      groupTitle = reminderGroup?.groupTitle,
      groupColor = reminderGroup?.groupColor ?: 0,
      version = reminder.versionId,
      syncState = SyncState.valueOf(reminder.syncState),
    )
  }
}
