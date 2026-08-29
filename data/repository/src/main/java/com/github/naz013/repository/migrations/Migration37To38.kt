package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.displayTitle
import com.github.naz013.domain.note.toJson

/** Replaces the `title`/`summary` split with a single formattable `text` + `spans` document -
 * see `NoteDocument`. Recreates the table (SQLite can't drop/retype columns on the versions this
 * app supports, see `Migration35To36`'s note), then, per row, promotes a non-blank `title` to a
 * heading-styled first line via [NoteDocument.fromLegacy] - this needs Kotlin-side JSON
 * construction the `INSERT...SELECT` step can't express, so it's filled in with a follow-up
 * cursor loop. Deliberately does not swallow failures the way most migrations in this file do:
 * a bug here loses note text, not just a schema no-op. */
internal val MIGRATION_37_38: Migration = object : Migration(37, 38) {
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
        `palette` INTEGER NOT NULL,
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
      INSERT INTO `Note` (`key`, `date`, `color`, `style`, `palette`, `uniqueId`, `updatedAt`,
        `opacity`, `fontSize`, `archived`, `isPinned`, `version`, `syncState`, `text`, `spans`,
        `displayTitle`)
      SELECT `key`, `date`, `color`, `style`, `palette`, `uniqueId`, `updatedAt`, `opacity`,
        `fontSize`, `archived`, `isPinned`, `version`, `syncState`, '', '[]', ''
      FROM `Note_old`
      """.trimIndent()
    )

    db.beginTransaction()
    try {
      db.query("SELECT `key`, `title`, `summary` FROM `Note_old`").use { cursor ->
        val keyIndex = cursor.getColumnIndexOrThrow("key")
        val titleIndex = cursor.getColumnIndexOrThrow("title")
        val summaryIndex = cursor.getColumnIndexOrThrow("summary")
        while (cursor.moveToNext()) {
          val key = cursor.getString(keyIndex)
          val title = cursor.getString(titleIndex) ?: ""
          val summary = cursor.getString(summaryIndex) ?: ""
          val document = NoteDocument.fromLegacy(title = title, summary = summary)
          db.execSQL(
            "UPDATE `Note` SET `text` = ?, `spans` = ?, `displayTitle` = ? WHERE `key` = ?",
            arrayOf(document.text, document.spans.toJson(), document.displayTitle(), key)
          )
        }
      }
      db.setTransactionSuccessful()
    } finally {
      db.endTransaction()
    }

    db.execSQL("DROP TABLE `Note_old`")
  }
}
