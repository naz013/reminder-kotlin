package com.elementary.tasks.core.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7: Migration = object : Migration(6, 7) {
  override fun migrate(database: SupportSQLiteDatabase) {
    runCatching {
      database.execSQL("DROP TABLE IF EXISTS SmsTemplate")
      database.execSQL("DROP TABLE IF EXISTS MissedCall")
    }
  }
}
