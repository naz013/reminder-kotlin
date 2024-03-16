package com.elementary.tasks.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.dao.CalendarEventsDao
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.dao.RecentQueryDao
import com.elementary.tasks.core.data.dao.RecurPresetDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.dao.UsedTimeDao
import com.elementary.tasks.core.data.migrations.MIGRATION_10_11
import com.elementary.tasks.core.data.migrations.MIGRATION_11_12
import com.elementary.tasks.core.data.migrations.MIGRATION_12_13
import com.elementary.tasks.core.data.migrations.MIGRATION_13_14
import com.elementary.tasks.core.data.migrations.MIGRATION_1_2
import com.elementary.tasks.core.data.migrations.MIGRATION_2_3
import com.elementary.tasks.core.data.migrations.MIGRATION_3_4
import com.elementary.tasks.core.data.migrations.MIGRATION_4_5
import com.elementary.tasks.core.data.migrations.MIGRATION_5_6
import com.elementary.tasks.core.data.migrations.MIGRATION_6_7
import com.elementary.tasks.core.data.migrations.MIGRATION_7_8
import com.elementary.tasks.core.data.migrations.MIGRATION_8_9
import com.elementary.tasks.core.data.migrations.MIGRATION_9_10
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.RecentQuery
import com.elementary.tasks.core.data.models.RecurPreset
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.models.UsedTime

@Database(
  entities = [
    Reminder::class,
    CalendarEvent::class,
    ReminderGroup::class,
    Note::class,
    Place::class,
    GoogleTaskList::class,
    GoogleTask::class,
    UsedTime::class,
    Birthday::class,
    ImageFile::class,
    RecurPreset::class,
    RecentQuery::class
  ],
  version = 14,
  exportSchema = false
)
abstract class AppDb : RoomDatabase() {

  abstract fun reminderDao(): ReminderDao
  abstract fun reminderGroupDao(): ReminderGroupDao
  abstract fun placesDao(): PlacesDao
  abstract fun calendarEventsDao(): CalendarEventsDao
  abstract fun notesDao(): NotesDao
  abstract fun birthdaysDao(): BirthdaysDao
  abstract fun googleTaskListsDao(): GoogleTaskListsDao
  abstract fun googleTasksDao(): GoogleTasksDao
  abstract fun usedTimeDao(): UsedTimeDao
  abstract fun recurPresetDao(): RecurPresetDao
  abstract fun recentQueryDao(): RecentQueryDao

  companion object {

    private var INSTANCE: AppDb? = null

    fun getAppDatabase(context: Context): AppDb {
      var instance = INSTANCE
      if (instance == null) {
        instance = Room.databaseBuilder(context.applicationContext, AppDb::class.java, "app_db")
          .addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13,
            MIGRATION_13_14
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
