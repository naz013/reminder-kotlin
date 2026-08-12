package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * SQLite can't relax a NOT NULL constraint via ALTER TABLE, so the notif_* columns (which need to
 * become nullable to hold per-reminder overrides instead of always-populated values) require the
 * standard Room recreate-table migration. Existing rows' previously-concrete notification values
 * are preserved as their initial override — no data loss, and semantically correct (what was
 * previously "the reminder's own settings" becomes "the reminder's explicit override").
 * notif_useGlobalSettings is dropped: per-field nullability now expresses what that flag did.
 */
internal val MIGRATION_23_24: Migration = object : Migration(23, 24) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL(
        "CREATE TABLE ReminderV2_new (" +
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
          "`notif_color` INTEGER, " +
          "`notif_vibrate` INTEGER, " +
          "`notif_vibrationPattern` TEXT, " +
          "`notif_repeatNotification` INTEGER, " +
          "`notif_volume` INTEGER, " +
          "`notif_soundUri` TEXT, " +
          "`notif_quietHoursFrom` TEXT, " +
          "`notif_quietHoursTo` TEXT, " +
          "`notif_activeHours` TEXT, " +
          "`notif_delayMinutes` INTEGER, " +
          "`notif_priority` TEXT, " +
          "`notif_category` TEXT, " +
          "`notif_bypassDoNotDisturb` INTEGER, " +
          "`notif_wakeScreen` INTEGER, " +
          "`notif_lockScreenVisibility` TEXT, " +
          "`notif_remindBefore` INTEGER, " +
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
        "INSERT INTO ReminderV2_new (" +
          "uuId, summary, description, noteId, groupId, recurrenceType, recurrencePayload, " +
          "sched_startDateTime, sched_eventDateTime, sched_updatedAt, " +
          "notif_color, notif_vibrate, notif_vibrationPattern, notif_repeatNotification, " +
          "notif_volume, notif_soundUri, notif_quietHoursFrom, notif_quietHoursTo, " +
          "notif_activeHours, notif_delayMinutes, notif_priority, notif_category, " +
          "notif_bypassDoNotDisturb, notif_wakeScreen, notif_lockScreenVisibility, notif_remindBefore, " +
          "cal_calendarId, cal_duration, cal_allDay, task_taskListId, " +
          "loc_isNotificationShown, loc_isLocked, loc_hasDelayedReminder, " +
          "actionType, actionTarget, actionSubject, attachmentFiles, places, shoppingItems, " +
          "builderScheme, uniqueId, isActive, isRemoved, eventCount, version, syncState" +
          ") SELECT " +
          "uuId, summary, description, noteId, groupId, recurrenceType, recurrencePayload, " +
          "sched_startDateTime, sched_eventDateTime, sched_updatedAt, " +
          "notif_color, notif_vibrate, notif_vibrationPattern, notif_repeatNotification, " +
          "notif_volume, notif_soundUri, notif_quietHoursFrom, notif_quietHoursTo, " +
          "notif_activeHours, notif_delayMinutes, notif_priority, notif_category, " +
          "notif_bypassDoNotDisturb, notif_wakeScreen, notif_lockScreenVisibility, notif_remindBefore, " +
          "cal_calendarId, cal_duration, cal_allDay, task_taskListId, " +
          "loc_isNotificationShown, loc_isLocked, loc_hasDelayedReminder, " +
          "actionType, actionTarget, actionSubject, attachmentFiles, places, shoppingItems, " +
          "builderScheme, uniqueId, isActive, isRemoved, eventCount, version, syncState" +
          " FROM ReminderV2"
      )
    }
    runCatching { db.execSQL("DROP TABLE ReminderV2") }
    runCatching { db.execSQL("ALTER TABLE ReminderV2_new RENAME TO ReminderV2") }

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
