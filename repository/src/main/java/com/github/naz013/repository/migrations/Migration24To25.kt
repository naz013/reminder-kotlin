package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_24_25: Migration = object : Migration(24, 25) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL(
        "CREATE TABLE GroupV2 (" +
          "`uuId` TEXT NOT NULL PRIMARY KEY, " +
          "`title` TEXT NOT NULL, " +
          "`color` INTEGER NOT NULL DEFAULT 0, " +
          "`isDefault` INTEGER NOT NULL DEFAULT 0, " +
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
          "`createdAt` INTEGER NOT NULL, " +
          "`version` INTEGER NOT NULL DEFAULT 0, " +
          "`syncState` TEXT NOT NULL DEFAULT 'WaitingForUpload')"
      )
    }
    runCatching {
      db.execSQL("CREATE INDEX index_GroupV2_isDefault ON GroupV2 (isDefault)")
    }
    runCatching {
      db.execSQL("CREATE INDEX index_GroupV2_syncState ON GroupV2 (syncState)")
    }
  }
}
