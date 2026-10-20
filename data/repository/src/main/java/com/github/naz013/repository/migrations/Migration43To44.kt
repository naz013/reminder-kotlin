package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the nullable `ReminderV2.iconIndex` and `ReminderV2.colorIndex` visual tag columns. */
internal val MIGRATION_43_44: Migration = object : Migration(43, 44) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE `ReminderV2` ADD COLUMN `iconIndex` INTEGER")
    db.execSQL("ALTER TABLE `ReminderV2` ADD COLUMN `colorIndex` INTEGER")
  }
}
