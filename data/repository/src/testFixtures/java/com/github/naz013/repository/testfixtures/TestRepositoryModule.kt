package com.github.naz013.repository.testfixtures

import android.content.Context
import androidx.room.Room
import com.github.naz013.repository.AppDb
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.repository.TagSyncTrigger
import com.github.naz013.repository.impl.GroupV2RepositoryImpl
import com.github.naz013.repository.impl.NoteRepositoryImpl
import com.github.naz013.repository.impl.ReminderV2RepositoryImpl
import com.github.naz013.repository.impl.TagAssignmentRepositoryImpl
import com.github.naz013.repository.impl.TagRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for instrumented tests: binds [ReminderV2Repository] and [GroupV2Repository]
 * against an in-memory [AppDb] instead of the on-device database, so instrumented tests round-trip
 * through the real repository/Room layer without touching device state between runs. `AppDb` and
 * the `*RepositoryImpl` classes are `internal` to this module, so this seam has to live here too -
 * it's published to other modules' `androidTest` source sets via Gradle test fixtures.
 *
 * Load after the production `repositoryModule` (see `KoinModule.kt`) so these bindings override
 * the real ones; everything else that module binds (e.g. `TableChangeNotifier`) is left as-is.
 */
fun testRepositoryModule(context: Context): Module = module {
  single {
    Room.inMemoryDatabaseBuilder(context, AppDb::class.java)
      .allowMainThreadQueries()
      .build()
  }
  factory<ReminderV2Repository> {
    ReminderV2RepositoryImpl(get<AppDb>().reminderV2Dao(), get())
  }
  factory<GroupV2Repository> {
    GroupV2RepositoryImpl(get<AppDb>().groupV2Dao(), get())
  }
  factory<NoteRepository> {
    NoteRepositoryImpl(get<AppDb>().notesDao(), get())
  }
  // TagSyncTrigger's only real implementation lives in `app` (schedules a cloud-sync upload) -
  // irrelevant to what these tests assert, so a no-op fake stands in for it here.
  factory<TagSyncTrigger> {
    object : TagSyncTrigger {
      override fun onTagSaved(id: String) = Unit

      override fun onTagDeleted(id: String) = Unit
    }
  }
  factory<TagRepository> {
    TagRepositoryImpl(get<AppDb>().tagDao(), get(), get())
  }
  factory<TagAssignmentRepository> {
    TagAssignmentRepositoryImpl(get<AppDb>().tagAssignmentDao(), get())
  }
}
