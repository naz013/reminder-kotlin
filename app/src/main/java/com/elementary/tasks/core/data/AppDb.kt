package com.elementary.tasks.core.data

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.elementary.tasks.core.data.dao.*
import com.elementary.tasks.core.data.models.*
import java.lang.Exception

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
], version = 3, exportSchema = false)
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
                try {
                    database.execSQL("DROP INDEX index_UsedTime_id")
                } catch (e: Exception) {
                }
            }
        }
        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("DROP INDEX index_UsedTime_timeMills")
                } catch (e: SQLiteException) {
                }
                try {
                    database.execSQL("DROP INDEX index_UsedTime_timeString")
                } catch (e: SQLiteException) {
                }
            }
        }

        fun getAppDatabase(context: Context): AppDb {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.applicationContext, AppDb::class.java, "app_db")
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .allowMainThreadQueries()
                        .build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
