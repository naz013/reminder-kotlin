package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the `PomodoroSession` table: a local-only history log of completed/interrupted Pomodoro
 * work intervals, optionally linked to a reminder, read by Streaks & Insights. */
internal val MIGRATION_41_42: Migration = object : Migration(41, 42) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `PomodoroSession` (
          `id` TEXT NOT NULL,
          `linkedReminderId` TEXT,
          `startedAt` INTEGER NOT NULL,
          `plannedFocusSeconds` INTEGER NOT NULL,
          `actualFocusSeconds` INTEGER NOT NULL,
          `wasCompleted` INTEGER NOT NULL,
          PRIMARY KEY(`id`)
        )
        """.trimIndent()
      )
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_PomodoroSession_linkedReminderId` ON `PomodoroSession` (`linkedReminderId`)")
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_PomodoroSession_startedAt` ON `PomodoroSession` (`startedAt`)")
    }
  }
}
