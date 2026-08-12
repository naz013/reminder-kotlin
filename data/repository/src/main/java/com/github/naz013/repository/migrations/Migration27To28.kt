package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_27_28: Migration = object : Migration(27, 28) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL("ALTER TABLE ReminderV2 ADD COLUMN snoozeCount INTEGER NOT NULL DEFAULT 0")
    }
    runCatching {
      db.execSQL("ALTER TABLE ReminderV2 ADD COLUMN lastShownAt INTEGER")
    }
  }
}
