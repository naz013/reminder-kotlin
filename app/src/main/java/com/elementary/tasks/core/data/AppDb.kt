package com.elementary.tasks.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.dao.CalendarEventsDao
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.dao.MissedCallsDao
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.dao.SmsTemplatesDao
import com.elementary.tasks.core.data.dao.UsedTimeDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.data.models.UsedTime

@Database(entities = [
    Reminder::class,
    CalendarEvent::class,
    ReminderGroup::class,
    MissedCall::class,
    Note::class,
    Place::class,
    GoogleTaskList::class,
    GoogleTask::class,
    UsedTime::class,
    Birthday::class,
    ImageFile::class,
    SmsTemplate::class
], version = 6, exportSchema = false)
abstract class AppDb : RoomDatabase() {

  abstract fun reminderDao(): ReminderDao
  abstract fun reminderGroupDao(): ReminderGroupDao
  abstract fun missedCallsDao(): MissedCallsDao
  abstract fun smsTemplatesDao(): SmsTemplatesDao
  abstract fun placesDao(): PlacesDao
  abstract fun calendarEventsDao(): CalendarEventsDao
  abstract fun notesDao(): NotesDao
  abstract fun birthdaysDao(): BirthdaysDao
  abstract fun googleTaskListsDao(): GoogleTaskListsDao
  abstract fun googleTasksDao(): GoogleTasksDao
  abstract fun usedTimeDao(): UsedTimeDao

  companion object {

    private var INSTANCE: AppDb? = null

    private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) {
        runCatching { database.execSQL("DROP INDEX index_UsedTime_id") }
      }
    }
    private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
      override fun migrate(database: SupportSQLiteDatabase) {
        runCatching { database.execSQL("DROP INDEX index_UsedTime_timeMills") }
        runCatching { database.execSQL("DROP INDEX index_UsedTime_timeString") }
      }
    }
    private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
      override fun migrate(database: SupportSQLiteDatabase) {
        runCatching { database.execSQL("ALTER TABLE Birthday ADD COLUMN updatedAt TEXT") }
        runCatching { database.execSQL("ALTER TABLE Note ADD COLUMN updatedAt TEXT") }
        runCatching {
          database.execSQL("ALTER TABLE Reminder ADD COLUMN eventState INTEGER DEFAULT 10 NOT NULL")
        }
        runCatching { database.execSQL("ALTER TABLE Reminder ADD COLUMN updatedAt TEXT") }
      }
    }
    private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
      override fun migrate(database: SupportSQLiteDatabase) {
        runCatching {
          database.execSQL("ALTER TABLE Reminder ADD COLUMN calendarId INTEGER DEFAULT 0 NOT NULL")
        }
      }
    }

    private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
      override fun migrate(database: SupportSQLiteDatabase) {
        runCatching {
          database.execSQL("ALTER TABLE Reminder ADD COLUMN taskListId TEXT")
        }
      }
    }

    fun getAppDatabase(context: Context): AppDb {
      var instance = INSTANCE
      if (instance == null) {
        instance = Room.databaseBuilder(context.applicationContext, AppDb::class.java, "app_db")
          .addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6
          )
          .allowMainThreadQueries()
          .build()
      }
      INSTANCE = instance
      return instance
    }

    fun destroyInstance() {
      INSTANCE = null
    }
  }
}
