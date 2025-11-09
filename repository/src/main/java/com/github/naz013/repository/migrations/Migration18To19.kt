package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_18_19: Migration = object : Migration(18, 19) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL(
        "CREATE TABLE EventOccurrence (`id` TEXT PRIMARY KEY NOT NULL, " +
          "`eventId` TEXT NOT NULL, " +
          "`date` INTEGER NOT NULL DEFAULT 0, " +
          "`time` INTEGER NOT NULL DEFAULT 0, " +
          "`type` TEXT NOT NULL)"
      )
    }
  }
}
