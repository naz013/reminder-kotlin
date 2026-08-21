package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_32_33: Migration = object : Migration(32, 33) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `Routine` (
          `id` TEXT NOT NULL,
          `title` TEXT NOT NULL,
          `description` TEXT,
          `color` INTEGER NOT NULL,
          `isPinned` INTEGER NOT NULL,
          `icon` TEXT,
          `steps` TEXT NOT NULL,
          `autoAdvance` INTEGER NOT NULL,
          `soundAlertsEnabled` INTEGER NOT NULL,
          `recurrenceType` TEXT NOT NULL,
          `recurrencePayload` TEXT NOT NULL,
          `reminderId` TEXT,
          `lastResetAt` INTEGER,
          `createdAt` INTEGER NOT NULL,
          `updatedAt` INTEGER NOT NULL,
          `version` INTEGER NOT NULL,
          `syncState` TEXT NOT NULL,
          PRIMARY KEY(`id`)
        )
        """.trimIndent()
      )
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_Routine_isPinned` ON `Routine` (`isPinned`)")
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_Routine_syncState` ON `Routine` (`syncState`)")

      db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `RoutineExecution` (
          `id` TEXT NOT NULL,
          `routineId` TEXT NOT NULL,
          `executedAt` INTEGER NOT NULL,
          `totalTimeSpentSeconds` INTEGER NOT NULL,
          `completedStepIds` TEXT NOT NULL,
          `totalStepsCount` INTEGER NOT NULL,
          PRIMARY KEY(`id`)
        )
        """.trimIndent()
      )
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_RoutineExecution_routineId` ON `RoutineExecution` (`routineId`)")
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_RoutineExecution_executedAt` ON `RoutineExecution` (`executedAt`)")
    }
  }
}
