package com.elementary.tasks.core.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10: Migration = object : Migration(9, 10) {
  override fun migrate(database: SupportSQLiteDatabase) {
    runCatching {
      database.execSQL("ALTER TABLE Note ADD COLUMN archived INTEGER DEFAULT 0 NOT NULL")
    }
  }
}
