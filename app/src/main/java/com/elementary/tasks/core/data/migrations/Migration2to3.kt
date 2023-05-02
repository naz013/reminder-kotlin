package com.elementary.tasks.core.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
  override fun migrate(database: SupportSQLiteDatabase) {
    runCatching { database.execSQL("DROP INDEX index_UsedTime_timeMills") }
    runCatching { database.execSQL("DROP INDEX index_UsedTime_timeString") }
  }
}
