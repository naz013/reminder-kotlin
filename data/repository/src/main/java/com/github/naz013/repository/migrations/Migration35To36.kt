package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Changes `Routine.icon` from a TEXT column to an INTEGER one - it's now an index into
 * `RoutineIconSet.ALL` (`ui-routine`) rather than a free-form string, and was never actually
 * populated by any UI before this (see the "unused field" note this replaces), so there's no
 * existing data worth carrying across - the new column is just left NULL for existing rows.
 * Recreates the table instead of `ALTER TABLE ... DROP COLUMN`/`ALTER COLUMN`, since SQLite has
 * no direct column-type-change statement and this app's minSdk (29) can ship with a bundled
 * SQLite older than the version that added `DROP COLUMN` support anyway (see `Migration34To35`). */
internal val MIGRATION_35_36: Migration = object : Migration(35, 36) {
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
          `icon` INTEGER,
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
        INSERT INTO `Routine` (id, title, description, color, isPinned, steps, autoAdvance,
          soundAlertsEnabled, recurrenceType, recurrencePayload, lastResetAt, createdAt, updatedAt,
          version, syncState)
        SELECT id, title, description, color, isPinned, steps, autoAdvance,
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
