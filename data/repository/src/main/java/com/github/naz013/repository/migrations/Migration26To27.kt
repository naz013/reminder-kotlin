package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_26_27: Migration = object : Migration(26, 27) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL("ALTER TABLE WorkflowRule ADD COLUMN templateId TEXT")
    }
    runCatching {
      db.execSQL(
        "CREATE TABLE WorkflowTemplate (" +
          "`id` TEXT NOT NULL PRIMARY KEY, " +
          "`title` TEXT NOT NULL, " +
          "`description` TEXT, " +
          "`category` TEXT NOT NULL, " +
          "`supportedScopeTypes` TEXT NOT NULL DEFAULT '', " +
          "`triggerType` TEXT NOT NULL, " +
          "`triggerPayload` TEXT NOT NULL, " +
          "`actionType` TEXT NOT NULL, " +
          "`actionPayload` TEXT NOT NULL, " +
          "`isBuiltIn` INTEGER NOT NULL DEFAULT 1, " +
          "`useCount` INTEGER NOT NULL DEFAULT 0, " +
          "`createdAt` INTEGER NOT NULL, " +
          "`version` INTEGER NOT NULL DEFAULT 0, " +
          "`syncState` TEXT NOT NULL DEFAULT 'WaitingForUpload')"
      )
    }
    runCatching {
      db.execSQL("CREATE INDEX index_WorkflowTemplate_category ON WorkflowTemplate (category)")
    }
    runCatching {
      db.execSQL("CREATE INDEX index_WorkflowTemplate_isBuiltIn ON WorkflowTemplate (isBuiltIn)")
    }
  }
}
