package com.elementary.tasks.core.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_12_13: Migration = object : Migration(12, 13) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching { db.execSQL("ALTER TABLE Reminder ADD COLUMN description TEXT") }
    runCatching { db.execSQL("ALTER TABLE Reminder ADD COLUMN builderScheme TEXT") }
    runCatching { db.execSQL("ALTER TABLE Reminder ADD COLUMN version TEXT") }

    runCatching {
      db.execSQL("ALTER TABLE RecurPreset ADD COLUMN type INTEGER DEFAULT 0 NOT NULL")
    }
    runCatching {
      db.execSQL("ALTER TABLE RecurPreset ADD COLUMN useCount INTEGER DEFAULT 0 NOT NULL")
    }
    runCatching {
      db.execSQL(
        "ALTER TABLE RecurPreset ADD COLUMN createdAt TEXT DEFAULT 'undefined' NOT NULL"
      )
    }
    runCatching {
      db.execSQL(
        "ALTER TABLE RecurPreset ADD COLUMN builderScheme TEXT DEFAULT 'undefined' NOT NULL"
      )
    }
  }
}
