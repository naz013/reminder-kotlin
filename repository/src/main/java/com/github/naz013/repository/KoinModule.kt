package com.github.naz013.repository

import com.github.naz013.repository.impl.BirthdayRepositoryImpl
import com.github.naz013.repository.impl.CalendarEventRepositoryImpl
import com.github.naz013.repository.impl.GoogleTaskListRepositoryImpl
import com.github.naz013.repository.impl.GoogleTaskRepositoryImpl
import com.github.naz013.repository.impl.NoteRepositoryImpl
import com.github.naz013.repository.impl.PlaceRepositoryImpl
import com.github.naz013.repository.impl.RecentQueryRepositoryImpl
import com.github.naz013.repository.impl.RecurPresetRepositoryImpl
import com.github.naz013.repository.impl.ReminderGroupRepositoryImpl
import com.github.naz013.repository.impl.ReminderRepositoryImpl
import com.github.naz013.repository.impl.RemoteFileMetadataRepositoryImpl
import com.github.naz013.repository.impl.UsedTimeRepositoryImpl
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.observer.TableChangeListenerFactoryImpl
import com.github.naz013.repository.observer.TableChangeNotifier
import org.koin.dsl.module

val repositoryModule = module {
  single { AppDb.getAppDatabase(get()) }

  factory { TableChangeNotifier(get()) }
  factory { TableChangeListenerFactoryImpl(get()) as TableChangeListenerFactory }

  factory { BirthdayRepositoryImpl(get<AppDb>().birthdaysDao(), get()) as BirthdayRepository }
  factory {
    RecentQueryRepositoryImpl(
      get<AppDb>().recentQueryDao(),
      get()
    ) as RecentQueryRepository
  }
  factory {
    RecurPresetRepositoryImpl(
      get<AppDb>().recurPresetDao(),
      get()
    ) as RecurPresetRepository
  }
  factory { UsedTimeRepositoryImpl(get<AppDb>().usedTimeDao(), get()) as UsedTimeRepository }
  factory {
    CalendarEventRepositoryImpl(
      get<AppDb>().calendarEventsDao(),
      get()
    ) as CalendarEventRepository
  }
  factory { PlaceRepositoryImpl(get<AppDb>().placesDao(), get()) as PlaceRepository }
  factory {
    ReminderGroupRepositoryImpl(
      get<AppDb>().reminderGroupDao(),
      get()
    ) as ReminderGroupRepository
  }
  factory {
    GoogleTaskListRepositoryImpl(
      get<AppDb>().googleTaskListsDao(),
      get()
    ) as GoogleTaskListRepository
  }
  factory { GoogleTaskRepositoryImpl(get<AppDb>().googleTasksDao(), get()) as GoogleTaskRepository }
  factory { NoteRepositoryImpl(get<AppDb>().notesDao(), get()) as NoteRepository }
  factory { ReminderRepositoryImpl(get<AppDb>().reminderDao(), get()) as ReminderRepository }
  factory { RemoteFileMetadataRepositoryImpl(get<AppDb>().remoteFileMetadataDao(), get()) as RemoteFileMetadataRepository }
}
