package com.elementary.tasks.core.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9: Migration = object : Migration(8, 9) {
  override fun migrate(database: SupportSQLiteDatabase) {
    runCatching {
      database.execSQL("ALTER TABLE Reminder ADD COLUMN recurDataObject TEXT")
    }
    runCatching {
      database.execSQL(
        "CREATE TABLE RecurPreset (`id` TEXT DEFAULT 'undefined' NOT NULL, " +
          "`recurObject` TEXT DEFAULT 'undefined' NOT NULL, `name` TEXT DEFAULT " +
          "'undefined' NOT NULL, PRIMARY KEY(`id`))"
      )
    }
  }
}
