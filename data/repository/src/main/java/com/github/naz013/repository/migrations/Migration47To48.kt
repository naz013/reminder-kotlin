package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the optional IANA time zone id to `ReminderV2`; null means floating (device zone). */
internal val MIGRATION_47_48: Migration = object : Migration(47, 48) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE `ReminderV2` ADD COLUMN `timeZoneId` TEXT")
  }
}
