package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_21_22: Migration = object : Migration(21, 22) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL(
        "CREATE TABLE ReminderV2 (" +
          "`uuId` TEXT NOT NULL PRIMARY KEY, " +
          "`summary` TEXT NOT NULL, " +
          "`description` TEXT, " +
          "`noteId` TEXT NOT NULL, " +
          "`groupId` TEXT, " +
          "`recurrenceType` TEXT NOT NULL, " +
          "`recurrencePayload` TEXT NOT NULL, " +
          "`sched_startDateTime` INTEGER NOT NULL, " +
          "`sched_eventDateTime` INTEGER, " +
          "`sched_updatedAt` INTEGER, " +
          "`notif_color` INTEGER NOT NULL DEFAULT 0, " +
          "`notif_vibrate` INTEGER NOT NULL DEFAULT 0, " +
          "`notif_repeatNotification` INTEGER NOT NULL DEFAULT 0, " +
          "`notif_volume` INTEGER NOT NULL DEFAULT -1, " +
          "`notif_useGlobalSettings` INTEGER NOT NULL DEFAULT 1, " +
          "`notif_quietHoursFrom` TEXT NOT NULL DEFAULT '', " +
          "`notif_quietHoursTo` TEXT NOT NULL DEFAULT '', " +
          "`notif_activeHours` TEXT NOT NULL DEFAULT '', " +
          "`notif_delayMinutes` INTEGER NOT NULL DEFAULT 0, " +
          "`notif_priority` TEXT NOT NULL DEFAULT 'NORMAL', " +
          "`notif_remindBefore` INTEGER NOT NULL DEFAULT 0, " +
          "`cal_calendarId` INTEGER, " +
          "`cal_duration` INTEGER, " +
          "`cal_allDay` INTEGER, " +
          "`task_taskListId` TEXT, " +
          "`loc_isNotificationShown` INTEGER, " +
          "`loc_isLocked` INTEGER, " +
          "`loc_hasDelayedReminder` INTEGER, " +
          "`actionType` TEXT NOT NULL DEFAULT 'NONE', " +
          "`actionTarget` TEXT NOT NULL DEFAULT '', " +
          "`actionSubject` TEXT NOT NULL DEFAULT '', " +
          "`attachmentFiles` TEXT NOT NULL DEFAULT '', " +
          "`places` TEXT NOT NULL DEFAULT '', " +
          "`shoppingItems` TEXT NOT NULL DEFAULT '', " +
          "`builderScheme` TEXT, " +
          "`uniqueId` INTEGER NOT NULL DEFAULT 0, " +
          "`isActive` INTEGER NOT NULL DEFAULT 1, " +
          "`isRemoved` INTEGER NOT NULL DEFAULT 0, " +
          "`eventCount` INTEGER NOT NULL DEFAULT 0, " +
          "`version` INTEGER NOT NULL DEFAULT 0, " +
          "`syncState` TEXT NOT NULL DEFAULT 'WaitingForUpload')"
      )
    }
    runCatching {
      db.execSQL(
        "CREATE INDEX index_ReminderV2_isActive_isRemoved_eventDateTime " +
          "ON ReminderV2 (isActive, isRemoved, sched_eventDateTime)"
      )
    }
    runCatching {
      db.execSQL("CREATE INDEX index_ReminderV2_recurrenceType ON ReminderV2 (recurrenceType)")
    }
    runCatching {
      db.execSQL("CREATE INDEX index_ReminderV2_groupId ON ReminderV2 (groupId)")
    }
    runCatching {
      db.execSQL("CREATE INDEX index_ReminderV2_noteId ON ReminderV2 (noteId)")
    }
    runCatching {
      db.execSQL("CREATE INDEX index_ReminderV2_syncState ON ReminderV2 (syncState)")
    }
  }
}
