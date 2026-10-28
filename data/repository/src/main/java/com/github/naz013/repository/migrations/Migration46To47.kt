package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the optional daily reminder time (seconds of day) and the only-if-not-done flag to `Habit`. */
internal val MIGRATION_46_47: Migration = object : Migration(46, 47) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE `Habit` ADD COLUMN `reminderTime` INTEGER")
    db.execSQL("ALTER TABLE `Habit` ADD COLUMN `remindOnlyIfNotDone` INTEGER NOT NULL DEFAULT 1")
  }
}
