package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_18_19: Migration = object : Migration(18, 19) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL(
        "CREATE TABLE EventOccurrence (`id` TEXT PRIMARY KEY NOT NULL, " +
          "`name` TEXT NOT NULL, " +
          "`lastModified` INTEGER NOT NULL DEFAULT 0, " +
          "`size` INTEGER NOT NULL DEFAULT 0, " +
          "`version` INTEGER NOT NULL DEFAULT 0, " +
          "`source` TEXT NOT NULL, " +
          "`fileExtension` TEXT NOT NULL, " +
          "`localUuId` TEXT, " +
          "`rev` TEXT NOT NULL)"
      )
    }
  }
}
