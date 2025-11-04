package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_17_18: Migration = object : Migration(17, 18) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL("ALTER TABLE RecurPreset ADD COLUMN version INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE RecurPreset ADD COLUMN syncState TEXT NOT NULL DEFAULT 'WaitingForUpload'")
    }
  }
}
