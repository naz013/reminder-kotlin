package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the nullable `ReminderV2.startAfterReminderId` chained-reminder link column and its index. */
internal val MIGRATION_44_45: Migration = object : Migration(44, 45) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE `ReminderV2` ADD COLUMN `startAfterReminderId` TEXT")
    db.execSQL(
      "CREATE INDEX IF NOT EXISTS `index_ReminderV2_startAfterReminderId` " +
        "ON `ReminderV2` (`startAfterReminderId`)"
    )
  }
}
