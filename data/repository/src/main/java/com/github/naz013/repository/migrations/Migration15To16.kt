package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_15_16: Migration = object : Migration(15, 16) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL("ALTER TABLE RecurPreset ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE RecurPreset ADD COLUMN recurItemsToAdd TEXT")
    }
  }
}
