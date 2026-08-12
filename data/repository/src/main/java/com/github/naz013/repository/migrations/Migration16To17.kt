package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_16_17: Migration = object : Migration(16, 17) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL(
        "CREATE TABLE RemoteFileMetadata (`id` TEXT PRIMARY KEY NOT NULL, " +
          "`name` TEXT NOT NULL, " +
          "`lastModified` INTEGER NOT NULL DEFAULT 0, " +
          "`size` INTEGER NOT NULL DEFAULT 0, " +
          "`version` INTEGER NOT NULL DEFAULT 0, " +
          "`source` TEXT NOT NULL, " +
          "`fileExtension` TEXT NOT NULL, " +
          "`localUuId` TEXT, " +
          "`rev` TEXT NOT NULL)"
      )
    }
    runCatching {
      db.execSQL("ALTER TABLE Birthday ADD COLUMN version INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE Birthday ADD COLUMN syncState TEXT NOT NULL DEFAULT 'WaitingForUpload'")
    }
    runCatching {
      db.execSQL("ALTER TABLE Note ADD COLUMN version INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE Note ADD COLUMN syncState TEXT NOT NULL DEFAULT 'WaitingForUpload'")
    }
    runCatching {
      db.execSQL("ALTER TABLE Place ADD COLUMN version INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE Place ADD COLUMN syncState TEXT NOT NULL DEFAULT 'WaitingForUpload'")
    }
    runCatching {
      db.execSQL("ALTER TABLE Reminder ADD COLUMN versionId INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE Reminder ADD COLUMN syncState TEXT NOT NULL DEFAULT 'WaitingForUpload'")
    }
    runCatching {
      db.execSQL("ALTER TABLE ReminderGroup ADD COLUMN version INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE ReminderGroup ADD COLUMN syncState TEXT NOT NULL DEFAULT 'WaitingForUpload'")
    }
  }
}
