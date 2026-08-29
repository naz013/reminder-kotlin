package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Drops the `palette` column: the app has shown every note color in one flat list for a while
 * now (see `NoteColorEngine.allColors()`), so the old (color, palette) split - where `color` was
 * a 0-based position within a 20-color group and `palette` picked the group - is dead weight.
 * Recreates the table (SQLite on this app's supported versions can't drop a column in place, see
 * `Migration35To36`'s note) folding the pair into one flat `color = palette * 20 + color`, the
 * same formula `combineLegacyNoteColor` uses for old backup/share files - see its doc for why 20.
 * Pure integer arithmetic, so unlike `Migration37To38` this needs no follow-up cursor loop. */
internal val MIGRATION_38_39: Migration = object : Migration(38, 39) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE `Note` RENAME TO `Note_old`")
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS `Note` (
        `text` TEXT NOT NULL,
        `spans` TEXT NOT NULL,
        `displayTitle` TEXT NOT NULL,
        `key` TEXT NOT NULL,
        `date` TEXT NOT NULL,
        `color` INTEGER NOT NULL,
        `style` INTEGER NOT NULL,
        `uniqueId` INTEGER NOT NULL,
        `updatedAt` TEXT,
        `opacity` INTEGER NOT NULL,
        `fontSize` INTEGER NOT NULL,
        `archived` INTEGER NOT NULL,
        `isPinned` INTEGER NOT NULL,
        `version` INTEGER NOT NULL,
        `syncState` TEXT NOT NULL,
        PRIMARY KEY(`key`)
      )
      """.trimIndent()
    )
    db.execSQL(
      """
      INSERT INTO `Note` (`text`, `spans`, `displayTitle`, `key`, `date`, `color`, `style`,
        `uniqueId`, `updatedAt`, `opacity`, `fontSize`, `archived`, `isPinned`, `version`,
        `syncState`)
      SELECT `text`, `spans`, `displayTitle`, `key`, `date`, (`palette` * 20 + `color`), `style`,
        `uniqueId`, `updatedAt`, `opacity`, `fontSize`, `archived`, `isPinned`, `version`,
        `syncState`
      FROM `Note_old`
      """.trimIndent()
    )
    db.execSQL("DROP TABLE `Note_old`")
  }
}
