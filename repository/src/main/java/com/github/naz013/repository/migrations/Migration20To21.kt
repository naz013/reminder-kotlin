package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_20_21: Migration = object : Migration(20, 21) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL("ALTER TABLE Note ADD COLUMN title TEXT NOT NULL DEFAULT ''")
      db.execSQL("ALTER TABLE Note ADD COLUMN titleFontSize INTEGER NOT NULL DEFAULT -1")
      db.execSQL("ALTER TABLE Note ADD COLUMN titleFontStyle INTEGER NOT NULL DEFAULT -1")
    }
  }
}
