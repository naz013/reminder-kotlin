package com.elementary.tasks.core.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8: Migration = object : Migration(7, 8) {
  override fun migrate(database: SupportSQLiteDatabase) {
    runCatching {
      database.execSQL("ALTER TABLE Note ADD COLUMN fontSize INTEGER DEFAULT -1 NOT NULL")
    }
  }
}
