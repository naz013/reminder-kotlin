package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_25_26: Migration = object : Migration(25, 26) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL(
        "CREATE TABLE WorkflowRule (" +
          "`uuId` TEXT NOT NULL PRIMARY KEY, " +
          "`title` TEXT NOT NULL, " +
          "`scopeType` TEXT NOT NULL, " +
          "`scopeId` TEXT, " +
          "`triggerType` TEXT NOT NULL, " +
          "`triggerPayload` TEXT NOT NULL, " +
          "`actionType` TEXT NOT NULL, " +
          "`actionPayload` TEXT NOT NULL, " +
          "`isEnabled` INTEGER NOT NULL DEFAULT 1, " +
          "`createdAt` INTEGER NOT NULL, " +
          "`lastRunAt` INTEGER, " +
          "`version` INTEGER NOT NULL DEFAULT 0, " +
          "`syncState` TEXT NOT NULL DEFAULT 'WaitingForUpload')"
      )
    }
    runCatching {
      db.execSQL("CREATE INDEX index_WorkflowRule_scopeType_scopeId ON WorkflowRule (scopeType, scopeId)")
    }
    runCatching {
      db.execSQL("CREATE INDEX index_WorkflowRule_triggerType ON WorkflowRule (triggerType)")
    }
    runCatching {
      db.execSQL("CREATE INDEX index_WorkflowRule_isEnabled ON WorkflowRule (isEnabled)")
    }
  }
}
