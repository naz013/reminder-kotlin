package com.github.naz013.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the `GoogleCalendarEvent` table: device-calendar events imported for read-only display
 * (month/timeline views), never converted into reminders. See `GoogleCalendarEventEntity`. */
internal val MIGRATION_39_40: Migration = object : Migration(39, 40) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS `GoogleCalendarEvent` (
        `deviceEventId` INTEGER NOT NULL,
        `calendarId` INTEGER NOT NULL,
        `calendarName` TEXT NOT NULL,
        `title` TEXT NOT NULL,
        `description` TEXT NOT NULL,
        `startDateTime` INTEGER NOT NULL,
        `endDateTime` INTEGER,
        `allDay` INTEGER NOT NULL,
        `rrule` TEXT NOT NULL,
        `isDismissed` INTEGER NOT NULL,
        `uuId` TEXT NOT NULL,
        `uniqueId` INTEGER NOT NULL,
        PRIMARY KEY(`uuId`)
      )
      """.trimIndent()
    )
    db.execSQL(
      "CREATE INDEX IF NOT EXISTS `index_GoogleCalendarEvent_deviceEventId` " +
        "ON `GoogleCalendarEvent` (`deviceEventId`)"
    )
    db.execSQL(
      "CREATE INDEX IF NOT EXISTS `index_GoogleCalendarEvent_isDismissed` " +
        "ON `GoogleCalendarEvent` (`isDismissed`)"
    )
  }
}
