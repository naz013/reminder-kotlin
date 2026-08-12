package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_5_6: Migration = object : Migration(5, 6) {
  override fun migrate(database: SupportSQLiteDatabase) {
    runCatching {
      database.execSQL("ALTER TABLE Reminder ADD COLUMN taskListId TEXT")
      database.execSQL("ALTER TABLE ImageFile ADD COLUMN filePath TEXT DEFAULT '' NOT NULL")
      database.execSQL("ALTER TABLE ImageFile ADD COLUMN fileName TEXT DEFAULT '' NOT NULL")
    }
  }
}
