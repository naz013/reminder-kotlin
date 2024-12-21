package com.github.naz013.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.naz013.repository.dao.BirthdaysDao
import com.github.naz013.repository.dao.CalendarEventsDao
import com.github.naz013.repository.dao.GoogleTaskListsDao
import com.github.naz013.repository.dao.GoogleTasksDao
import com.github.naz013.repository.dao.NotesDao
import com.github.naz013.repository.dao.PlacesDao
import com.github.naz013.repository.dao.RecentQueryDao
import com.github.naz013.repository.dao.RecurPresetDao
import com.github.naz013.repository.dao.ReminderDao
import com.github.naz013.repository.dao.ReminderGroupDao
import com.github.naz013.repository.dao.UsedTimeDao
import com.github.naz013.repository.entity.BirthdayEntity
import com.github.naz013.repository.entity.CalendarEventEntity
import com.github.naz013.repository.entity.GoogleTaskEntity
import com.github.naz013.repository.entity.GoogleTaskListEntity
import com.github.naz013.repository.entity.ImageFileEntity
import com.github.naz013.repository.entity.NoteEntity
import com.github.naz013.repository.entity.PlaceEntity
import com.github.naz013.repository.entity.RecentQueryEntity
import com.github.naz013.repository.entity.RecurPresetEntity
import com.github.naz013.repository.entity.ReminderEntity
import com.github.naz013.repository.entity.ReminderGroupEntity
import com.github.naz013.repository.entity.UsedTimeEntity
import com.github.naz013.repository.migrations.MIGRATION_10_11
import com.github.naz013.repository.migrations.MIGRATION_11_12
import com.github.naz013.repository.migrations.MIGRATION_12_13
import com.github.naz013.repository.migrations.MIGRATION_13_14
import com.github.naz013.repository.migrations.MIGRATION_14_15
import com.github.naz013.repository.migrations.MIGRATION_1_2
import com.github.naz013.repository.migrations.MIGRATION_2_3
import com.github.naz013.repository.migrations.MIGRATION_3_4
import com.github.naz013.repository.migrations.MIGRATION_4_5
import com.github.naz013.repository.migrations.MIGRATION_5_6
import com.github.naz013.repository.migrations.MIGRATION_6_7
import com.github.naz013.repository.migrations.MIGRATION_7_8
import com.github.naz013.repository.migrations.MIGRATION_8_9
import com.github.naz013.repository.migrations.MIGRATION_9_10

@Database(
  entities = [
    ReminderEntity::class,
    CalendarEventEntity::class,
    ReminderGroupEntity::class,
    NoteEntity::class,
    PlaceEntity::class,
    GoogleTaskListEntity::class,
    GoogleTaskEntity::class,
    UsedTimeEntity::class,
    BirthdayEntity::class,
    ImageFileEntity::class,
    RecurPresetEntity::class,
    RecentQueryEntity::class
  ],
  version = 15,
  exportSchema = false
)
internal abstract class AppDb : RoomDatabase() {

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
            MIGRATION_13_14,
            MIGRATION_14_15
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
