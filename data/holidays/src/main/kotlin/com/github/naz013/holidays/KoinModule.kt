package com.github.naz013.holidays

import com.github.naz013.holidays.remote.HolidayFirestoreDataSource
import com.github.naz013.holidays.work.HolidaySyncTask
import com.github.naz013.holidaysapi.HolidaySyncScheduler
import com.github.naz013.workapi.BackgroundTask
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.qualifier.named
import org.koin.dsl.module

val holidaysModule = module {
  // Named database, not the project's (default) one - see PUBLIC_HOLIDAY_INTEGRATION_PLAN.md
  // ("Context" section) for why: the default Firestore database was disabled (linked to a
  // disabled App Engine app), so the pipeline writes to a separate "holidays" database instead.
  single<FirebaseFirestore> { FirebaseFirestore.getInstance(FirebaseApp.getInstance(), FIRESTORE_DATABASE_ID) }

  factory { HolidayFirestoreDataSource(get()) }
  single<HolidaySyncScheduler> { HolidaySyncSchedulerImpl(get()) }

  factory<BackgroundTask>(named(HolidaySyncTask.TASK_KEY)) { HolidaySyncTask(get(), get(), get()) }
}

private const val FIRESTORE_DATABASE_ID = "holidays"
