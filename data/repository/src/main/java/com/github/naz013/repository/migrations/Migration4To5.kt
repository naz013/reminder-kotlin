package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_4_5: Migration = object : Migration(4, 5) {
  override fun migrate(database: SupportSQLiteDatabase) {
    runCatching {
      database.execSQL("ALTER TABLE Reminder ADD COLUMN calendarId INTEGER DEFAULT 0 NOT NULL")
    }
  }
}
