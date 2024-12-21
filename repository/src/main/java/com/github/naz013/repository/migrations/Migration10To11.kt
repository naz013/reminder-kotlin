package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_10_11: Migration = object : Migration(10, 11) {
  override fun migrate(database: SupportSQLiteDatabase) {
    runCatching {
      database.execSQL("ALTER TABLE Birthday ADD COLUMN ignoreYear INTEGER DEFAULT 0 NOT NULL")
    }
    runCatching {
      database.execSQL("ALTER TABLE CalendarEvent ADD COLUMN allDay INTEGER DEFAULT 0 NOT NULL")
    }
    runCatching {
      database.execSQL("ALTER TABLE Reminder ADD COLUMN allDay INTEGER DEFAULT 0 NOT NULL")
    }
  }
}
