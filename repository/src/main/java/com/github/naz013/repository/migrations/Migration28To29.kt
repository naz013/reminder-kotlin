package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_28_29: Migration = object : Migration(28, 29) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL("ALTER TABLE WorkflowRule ADD COLUMN conditionsPayload TEXT NOT NULL DEFAULT '[]'")
    }
  }
}
