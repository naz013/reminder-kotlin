package com.elementary.tasks.core.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_13_14: Migration = object : Migration(13, 14) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL("ALTER TABLE GoogleTask ADD COLUMN uploaded INTEGER DEFAULT 1 NOT NULL")
    }
    runCatching {
      db.execSQL("ALTER TABLE GoogleTaskList ADD COLUMN uploaded INTEGER DEFAULT 1 NOT NULL")
    }
  }
}
