package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_29_30: Migration = object : Migration(29, 30) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL(
        "CREATE TABLE Tag (" +
          "`id` TEXT NOT NULL PRIMARY KEY, " +
          "`name` TEXT NOT NULL, " +
          "`color` INTEGER NOT NULL, " +
          "`version` INTEGER NOT NULL, " +
          "`syncState` TEXT NOT NULL)"
      )
    }
    runCatching {
      db.execSQL("CREATE INDEX index_Tag_syncState ON Tag (syncState)")
    }
    runCatching {
      db.execSQL(
        "CREATE TABLE TagAssignment (" +
          "`tagId` TEXT NOT NULL, " +
          "`itemId` TEXT NOT NULL, " +
          "`itemType` TEXT NOT NULL, " +
          "PRIMARY KEY(`tagId`, `itemId`, `itemType`))"
      )
    }
    runCatching {
      db.execSQL("CREATE INDEX index_TagAssignment_itemId_itemType ON TagAssignment (itemId, itemType)")
    }
    runCatching {
      db.execSQL("CREATE INDEX index_TagAssignment_tagId ON TagAssignment (tagId)")
    }
  }
}
