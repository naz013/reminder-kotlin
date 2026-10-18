package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the `Habit` and `HabitCheckIn` tables for the Habit Tracker feature. */
internal val MIGRATION_42_43: Migration = object : Migration(42, 43) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `Habit` (
          `id` TEXT NOT NULL,
          `name` TEXT NOT NULL,
          `description` TEXT,
          `frequency` TEXT NOT NULL,
          `targetCount` INTEGER NOT NULL,
          `color` INTEGER NOT NULL,
          `icon` INTEGER,
          `isPinned` INTEGER NOT NULL,
          `createdAt` INTEGER NOT NULL,
          `updatedAt` INTEGER NOT NULL,
          `version` INTEGER NOT NULL,
          `syncState` TEXT NOT NULL,
          PRIMARY KEY(`id`)
        )
        """.trimIndent()
      )
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_Habit_isPinned` ON `Habit` (`isPinned`)")
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_Habit_syncState` ON `Habit` (`syncState`)")
      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `HabitCheckIn` (
          `habitId` TEXT NOT NULL,
          `date` TEXT NOT NULL,
          `count` INTEGER NOT NULL,
          `isCompleted` INTEGER NOT NULL,
          `createdAt` INTEGER NOT NULL,
          `updatedAt` INTEGER NOT NULL,
          PRIMARY KEY(`habitId`, `date`)
        )
        """.trimIndent()
      )
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_HabitCheckIn_date` ON `HabitCheckIn` (`date`)")
    }
  }
}
