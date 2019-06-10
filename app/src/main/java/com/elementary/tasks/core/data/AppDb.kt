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
    ReminderChain::class,
    SmsTemplate::class
], version = 4, exportSchema = false)
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
    abstract fun reminderChainDao(): ReminderChainDao

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
        private val MIGRATION_1_3: Migration = object : Migration(1, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("DROP INDEX index_UsedTime_id")
                } catch (e: Exception) {
                }
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
        private val MIGRATION_1_4: Migration = object : Migration(1, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("DROP INDEX index_UsedTime_id")
                } catch (e: Exception) {
                }
                try {
                    database.execSQL("DROP INDEX index_UsedTime_timeMills")
                } catch (e: SQLiteException) {
                }
                try {
                    database.execSQL("DROP INDEX index_UsedTime_timeString")
                } catch (e: SQLiteException) {
                }
                database.execSQL("""CREATE TABLE IF NOT EXISTS ReminderChain (uuId TEXT NOT NULL,
                        previousId TEXT NOT NULL,
                        nextId TEXT NOT NULL,
                        gmtTime TEXT NOT NULL,
                        activationType INTEGER DEFAULT 0 NOT NULL,
                        PRIMARY KEY(uuId))""")

                database.execSQL("ALTER TABLE Reminder ADD COLUMN eventState INTEGER DEFAULT 10 NOT NULL")
            }
        }
        private val MIGRATION_2_4: Migration = object : Migration(2, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("DROP INDEX index_UsedTime_timeMills")
                } catch (e: SQLiteException) {
                }
                try {
                    database.execSQL("DROP INDEX index_UsedTime_timeString")
                } catch (e: SQLiteException) {
                }
                database.execSQL("""CREATE TABLE IF NOT EXISTS ReminderChain (uuId TEXT NOT NULL,
                        previousId TEXT NOT NULL,
                        nextId TEXT NOT NULL,
                        gmtTime TEXT NOT NULL,
                        activationType INTEGER DEFAULT 0 NOT NULL,
                        PRIMARY KEY(uuId))""")

                database.execSQL("ALTER TABLE Reminder ADD COLUMN eventState INTEGER DEFAULT 10 NOT NULL")
            }
        }
        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS ReminderChain (uuId TEXT NOT NULL,
                        previousId TEXT NOT NULL,
                        nextId TEXT NOT NULL,
                        gmtTime TEXT NOT NULL,
                        activationType INTEGER DEFAULT 0 NOT NULL,
                        PRIMARY KEY(uuId))""")

                database.execSQL("ALTER TABLE Reminder ADD COLUMN eventState INTEGER DEFAULT 10 NOT NULL")
            }
        }

        fun getAppDatabase(context: Context): AppDb {
            var instance = INSTANCE
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext, AppDb::class.java, "app_db")
                        .addMigrations(
                                MIGRATION_1_2,
                                MIGRATION_1_3, MIGRATION_2_3,
                                MIGRATION_1_4, MIGRATION_2_4, MIGRATION_3_4
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
