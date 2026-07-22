package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_22_23: Migration = object : Migration(22, 23) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL("ALTER TABLE ReminderV2 ADD COLUMN notif_vibrationPattern TEXT")
    }
    runCatching {
      db.execSQL("ALTER TABLE ReminderV2 ADD COLUMN notif_soundUri TEXT")
    }
    runCatching {
      db.execSQL(
        "ALTER TABLE ReminderV2 ADD COLUMN notif_category TEXT NOT NULL DEFAULT 'DEFAULT'"
      )
    }
    runCatching {
      db.execSQL(
        "ALTER TABLE ReminderV2 ADD COLUMN notif_bypassDoNotDisturb INTEGER NOT NULL DEFAULT 0"
      )
    }
    runCatching {
      db.execSQL(
        "ALTER TABLE ReminderV2 ADD COLUMN notif_wakeScreen INTEGER NOT NULL DEFAULT 0"
      )
    }
    runCatching {
      db.execSQL(
        "ALTER TABLE ReminderV2 ADD COLUMN notif_lockScreenVisibility TEXT NOT NULL DEFAULT 'PRIVATE'"
      )
    }
  }
}
