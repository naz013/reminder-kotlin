package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Drops the unused `Routine.reminderId` column. Recreates the table instead of
 * `ALTER TABLE ... DROP COLUMN`, since that syntax needs SQLite 3.35+ and this app's minSdk (29)
 * can ship with an older bundled SQLite version. */
internal val MIGRATION_34_35: Migration = object : Migration(34, 35) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL("ALTER TABLE `Routine` RENAME TO `Routine_old`")
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
          `lastResetAt` INTEGER,
          `createdAt` INTEGER NOT NULL,
          `updatedAt` INTEGER NOT NULL,
          `version` INTEGER NOT NULL,
          `syncState` TEXT NOT NULL,
          PRIMARY KEY(`id`)
        )
        """.trimIndent()
      )
      db.execSQL(
        """
        INSERT INTO `Routine` (id, title, description, color, isPinned, icon, steps, autoAdvance,
          soundAlertsEnabled, recurrenceType, recurrencePayload, lastResetAt, createdAt, updatedAt,
          version, syncState)
        SELECT id, title, description, color, isPinned, icon, steps, autoAdvance,
          soundAlertsEnabled, recurrenceType, recurrencePayload, lastResetAt, createdAt, updatedAt,
          version, syncState
        FROM `Routine_old`
        """.trimIndent()
      )
      db.execSQL("DROP TABLE `Routine_old`")
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_Routine_isPinned` ON `Routine` (`isPinned`)")
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_Routine_syncState` ON `Routine` (`syncState`)")
    }
  }
}
