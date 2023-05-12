package com.elementary.tasks.core.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
  override fun migrate(database: SupportSQLiteDatabase) {
    runCatching { database.execSQL("ALTER TABLE Birthday ADD COLUMN updatedAt TEXT") }
    runCatching { database.execSQL("ALTER TABLE Note ADD COLUMN updatedAt TEXT") }
    runCatching {
      database.execSQL("ALTER TABLE Reminder ADD COLUMN eventState INTEGER DEFAULT 10 NOT NULL")
    }
    runCatching { database.execSQL("ALTER TABLE Reminder ADD COLUMN updatedAt TEXT") }
  }
}
