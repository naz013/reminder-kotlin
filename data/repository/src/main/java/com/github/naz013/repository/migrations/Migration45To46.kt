package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the local-only `NoteVersion` table holding previous note content snapshots. */
internal val MIGRATION_45_46: Migration = object : Migration(45, 46) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
      "CREATE TABLE IF NOT EXISTS `NoteVersion` (" +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
        "`noteKey` TEXT NOT NULL, " +
        "`text` TEXT NOT NULL, " +
        "`spans` TEXT NOT NULL, " +
        "`savedAt` INTEGER NOT NULL)"
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_NoteVersion_noteKey` ON `NoteVersion` (`noteKey`)")
  }
}
