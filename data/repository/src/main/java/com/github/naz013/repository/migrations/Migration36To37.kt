package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_36_37: Migration = object : Migration(36, 37) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL("ALTER TABLE `ReminderV2` ADD COLUMN `offlineOnly` INTEGER NOT NULL DEFAULT 0")
    }
  }
}
