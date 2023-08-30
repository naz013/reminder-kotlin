package com.elementary.tasks.core.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_11_12: Migration = object : Migration(11, 12) {
  override fun migrate(database: SupportSQLiteDatabase) {
    runCatching {
      database.execSQL(
        "CREATE TABLE RecentQuery (`id` INTEGER PRIMARY KEY NOT NULL, " +
          "`queryType` TEXT DEFAULT 'undefined' NOT NULL, " +
          "`queryText` TEXT DEFAULT 'undefined' NOT NULL, " +
          "`lastUsedAt` TEXT DEFAULT 'undefined' NOT NULL, " +
          "`targetId` TEXT, " +
          "`targetType` TEXT)"
      )
    }
  }
}
