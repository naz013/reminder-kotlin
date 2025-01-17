package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_1_2: Migration = object : Migration(1, 2) {
  override fun migrate(database: SupportSQLiteDatabase) {
    runCatching { database.execSQL("DROP INDEX index_UsedTime_id") }
  }
}
