package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds `ReminderV2.isCritical`: opt-in alarm-clock-style escalation (repeats, bypasses Do Not
 * Disturb, wakes the screen) for reminders the user can't afford to miss. */
internal val MIGRATION_40_41: Migration = object : Migration(40, 41) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE `ReminderV2` ADD COLUMN `isCritical` INTEGER NOT NULL DEFAULT 0")
  }
}
