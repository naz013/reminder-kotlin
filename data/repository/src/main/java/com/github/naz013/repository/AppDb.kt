package com.github.naz013.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.naz013.repository.dao.BirthdaysDao
import com.github.naz013.repository.dao.CalendarEventsDao
import com.github.naz013.repository.dao.EventHistoryDao
import com.github.naz013.repository.dao.EventOccurrenceDao
import com.github.naz013.repository.dao.GoogleCalendarEventDao
import com.github.naz013.repository.dao.GoogleTaskListsDao
import com.github.naz013.repository.dao.GoogleTasksDao
import com.github.naz013.repository.dao.GroupV2Dao
import com.github.naz013.repository.dao.HolidayDao
import com.github.naz013.repository.dao.NotesDao
import com.github.naz013.repository.dao.PlacesDao
import com.github.naz013.repository.dao.RecentQueryDao
import com.github.naz013.repository.dao.RecurPresetDao
import com.github.naz013.repository.dao.ReminderDao
import com.github.naz013.repository.dao.ReminderGroupDao
import com.github.naz013.repository.dao.ReminderV2Dao
import com.github.naz013.repository.dao.RemoteFileMetadataDao
import com.github.naz013.repository.dao.RoutineDao
import com.github.naz013.repository.dao.RoutineExecutionDao
import com.github.naz013.repository.dao.TagAssignmentDao
import com.github.naz013.repository.dao.TagDao
import com.github.naz013.repository.dao.UsedTimeDao
import com.github.naz013.repository.dao.WorkflowRuleDao
import com.github.naz013.repository.dao.WorkflowTemplateDao
import com.github.naz013.repository.entity.BirthdayEntity
import com.github.naz013.repository.entity.CalendarEventEntity
import com.github.naz013.repository.entity.EventHistoryEntity
import com.github.naz013.repository.entity.EventOccurrenceEntity
import com.github.naz013.repository.entity.GoogleCalendarEventEntity
import com.github.naz013.repository.entity.GoogleTaskEntity
import com.github.naz013.repository.entity.GoogleTaskListEntity
import com.github.naz013.repository.entity.GroupV2Entity
import com.github.naz013.repository.entity.HolidayEntity
import com.github.naz013.repository.entity.ImageFileEntity
import com.github.naz013.repository.entity.NoteEntity
import com.github.naz013.repository.entity.PlaceEntity
import com.github.naz013.repository.entity.RecentQueryEntity
import com.github.naz013.repository.entity.RecurPresetEntity
import com.github.naz013.repository.entity.ReminderEntity
import com.github.naz013.repository.entity.ReminderGroupEntity
import com.github.naz013.repository.entity.ReminderV2Entity
import com.github.naz013.repository.entity.RemoteFileMetadataEntity
import com.github.naz013.repository.entity.RoutineEntity
import com.github.naz013.repository.entity.RoutineExecutionEntity
import com.github.naz013.repository.entity.TagAssignmentEntity
import com.github.naz013.repository.entity.TagEntity
import com.github.naz013.repository.entity.UsedTimeEntity
import com.github.naz013.repository.entity.WorkflowRuleEntity
import com.github.naz013.repository.entity.WorkflowTemplateEntity
import com.github.naz013.repository.migrations.MIGRATION_10_11
import com.github.naz013.repository.migrations.MIGRATION_11_12
import com.github.naz013.repository.migrations.MIGRATION_12_13
import com.github.naz013.repository.migrations.MIGRATION_13_14
import com.github.naz013.repository.migrations.MIGRATION_14_15
import com.github.naz013.repository.migrations.MIGRATION_15_16
import com.github.naz013.repository.migrations.MIGRATION_16_17
import com.github.naz013.repository.migrations.MIGRATION_17_18
import com.github.naz013.repository.migrations.MIGRATION_18_19
import com.github.naz013.repository.migrations.MIGRATION_19_20
import com.github.naz013.repository.migrations.MIGRATION_1_2
import com.github.naz013.repository.migrations.MIGRATION_20_21
import com.github.naz013.repository.migrations.MIGRATION_21_22
import com.github.naz013.repository.migrations.MIGRATION_22_23
import com.github.naz013.repository.migrations.MIGRATION_23_24
import com.github.naz013.repository.migrations.MIGRATION_24_25
import com.github.naz013.repository.migrations.MIGRATION_25_26
import com.github.naz013.repository.migrations.MIGRATION_26_27
import com.github.naz013.repository.migrations.MIGRATION_27_28
import com.github.naz013.repository.migrations.MIGRATION_28_29
import com.github.naz013.repository.migrations.MIGRATION_29_30
import com.github.naz013.repository.migrations.MIGRATION_2_3
import com.github.naz013.repository.migrations.MIGRATION_30_31
import com.github.naz013.repository.migrations.MIGRATION_31_32
import com.github.naz013.repository.migrations.MIGRATION_32_33
import com.github.naz013.repository.migrations.MIGRATION_33_34
import com.github.naz013.repository.migrations.MIGRATION_34_35
import com.github.naz013.repository.migrations.MIGRATION_35_36
import com.github.naz013.repository.migrations.MIGRATION_36_37
import com.github.naz013.repository.migrations.MIGRATION_37_38
import com.github.naz013.repository.migrations.MIGRATION_38_39
import com.github.naz013.repository.migrations.MIGRATION_39_40
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
    RecentQueryEntity::class,
    RemoteFileMetadataEntity::class,
    EventOccurrenceEntity::class,
    EventHistoryEntity::class,
    ReminderV2Entity::class,
    GroupV2Entity::class,
    WorkflowRuleEntity::class,
    WorkflowTemplateEntity::class,
    TagEntity::class,
    TagAssignmentEntity::class,
    HolidayEntity::class,
    RoutineEntity::class,
    RoutineExecutionEntity::class,
    GoogleCalendarEventEntity::class
  ],
  version = 40,
  exportSchema = false
)
@Suppress("TooManyFunctions") // one DAO accessor per entity - inherent to this class, not a smell
internal abstract class AppDb : RoomDatabase() {

  abstract fun reminderDao(): ReminderDao
  abstract fun reminderGroupDao(): ReminderGroupDao
  abstract fun reminderV2Dao(): ReminderV2Dao
  abstract fun groupV2Dao(): GroupV2Dao
  abstract fun workflowRuleDao(): WorkflowRuleDao
  abstract fun workflowTemplateDao(): WorkflowTemplateDao
  abstract fun placesDao(): PlacesDao
  abstract fun calendarEventsDao(): CalendarEventsDao
  abstract fun notesDao(): NotesDao
  abstract fun birthdaysDao(): BirthdaysDao
  abstract fun googleTaskListsDao(): GoogleTaskListsDao
  abstract fun googleTasksDao(): GoogleTasksDao
  abstract fun usedTimeDao(): UsedTimeDao
  abstract fun recurPresetDao(): RecurPresetDao
  abstract fun recentQueryDao(): RecentQueryDao
  abstract fun remoteFileMetadataDao(): RemoteFileMetadataDao
  abstract fun eventOccurrenceDao(): EventOccurrenceDao
  abstract fun eventHistoryDao(): EventHistoryDao
  abstract fun tagDao(): TagDao
  abstract fun tagAssignmentDao(): TagAssignmentDao
  abstract fun holidayDao(): HolidayDao
  abstract fun routineDao(): RoutineDao
  abstract fun routineExecutionDao(): RoutineExecutionDao
  abstract fun googleCalendarEventDao(): GoogleCalendarEventDao

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
            MIGRATION_14_15,
            MIGRATION_15_16,
            MIGRATION_16_17,
            MIGRATION_17_18,
            MIGRATION_18_19,
            MIGRATION_19_20,
            MIGRATION_20_21,
            MIGRATION_21_22,
            MIGRATION_22_23,
            MIGRATION_23_24,
            MIGRATION_24_25,
            MIGRATION_25_26,
            MIGRATION_26_27,
            MIGRATION_27_28,
            MIGRATION_28_29,
            MIGRATION_29_30,
            MIGRATION_30_31,
            MIGRATION_31_32,
            MIGRATION_32_33,
            MIGRATION_33_34,
            MIGRATION_34_35,
            MIGRATION_35_36,
            MIGRATION_36_37,
            MIGRATION_37_38,
            MIGRATION_38_39,
            MIGRATION_39_40
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
