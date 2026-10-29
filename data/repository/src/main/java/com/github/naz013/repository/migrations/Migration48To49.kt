package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the event type (birthday, anniversary, ...) and an optional custom label to `Birthday`. */
internal val MIGRATION_48_49: Migration = object : Migration(48, 49) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE `Birthday` ADD COLUMN `eventType` TEXT NOT NULL DEFAULT 'BIRTHDAY'")
    db.execSQL("ALTER TABLE `Birthday` ADD COLUMN `customLabel` TEXT")
  }
}
