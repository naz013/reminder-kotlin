package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_30_31: Migration = object : Migration(30, 31) {
  override fun migrate(db: SupportSQLiteDatabase) {
    runCatching {
      db.execSQL(
        "CREATE TABLE Holiday (" +
          "`id` TEXT NOT NULL PRIMARY KEY, " +
          "`countryCode` TEXT NOT NULL, " +
          "`year` INTEGER NOT NULL, " +
          "`date` INTEGER NOT NULL, " +
          "`name` TEXT NOT NULL, " +
          "`nameLocal` TEXT, " +
          "`type` TEXT NOT NULL, " +
          "`location` TEXT)"
      )
    }
    runCatching {
      db.execSQL("CREATE INDEX index_Holiday_countryCode_date ON Holiday (countryCode, date)")
    }
    runCatching {
      db.execSQL("CREATE INDEX index_Holiday_countryCode_year ON Holiday (countryCode, year)")
    }
  }
}
