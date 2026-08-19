package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_31_32: Migration = object : Migration(31, 32) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL("ALTER TABLE ReminderV2 ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
    }
  }
}
