package com.github.naz013.repository

import com.github.naz013.repository.impl.BirthdayRepositoryImpl
import com.github.naz013.repository.impl.CalendarEventRepositoryImpl
import com.github.naz013.repository.impl.EventHistoryRepositoryImpl
import com.github.naz013.repository.impl.EventOccurrenceRepositoryImpl
import com.github.naz013.repository.impl.GoogleTaskListRepositoryImpl
import com.github.naz013.repository.impl.GoogleTaskRepositoryImpl
import com.github.naz013.repository.impl.GroupV2RepositoryImpl
import com.github.naz013.repository.impl.HolidayRepositoryImpl
import com.github.naz013.repository.impl.NoteRepositoryImpl
import com.github.naz013.repository.impl.PlaceRepositoryImpl
import com.github.naz013.repository.impl.RecentQueryRepositoryImpl
import com.github.naz013.repository.impl.RecurPresetRepositoryImpl
import com.github.naz013.repository.impl.ReminderGroupRepositoryImpl
import com.github.naz013.repository.impl.ReminderRepositoryImpl
import com.github.naz013.repository.impl.ReminderV2RepositoryImpl
import com.github.naz013.repository.impl.RemoteFileMetadataRepositoryImpl
import com.github.naz013.repository.impl.TagAssignmentRepositoryImpl
import com.github.naz013.repository.impl.TagRepositoryImpl
import com.github.naz013.repository.impl.UsedTimeRepositoryImpl
import com.github.naz013.repository.impl.WorkflowRuleRepositoryImpl
import com.github.naz013.repository.impl.WorkflowTemplateRepositoryImpl
import com.github.naz013.repository.migration.GroupV2BackfillUseCase
import com.github.naz013.repository.migration.ReminderGroupRepository
import com.github.naz013.repository.migration.ReminderRepository
import com.github.naz013.repository.migration.ReminderV2BackfillUseCase
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.observer.TableChangeListenerFactoryImpl
import com.github.naz013.repository.observer.TableChangeNotifier
import org.koin.dsl.module

val repositoryModule = module {
  single { AppDb.getAppDatabase(get()) }

  factory { TableChangeNotifier(get()) }
  factory { TableChangeListenerFactoryImpl(get()) as TableChangeListenerFactory }

  factory { BirthdayRepositoryImpl(get<AppDb>().birthdaysDao(), get()) as BirthdayRepository }
  factory { TagRepositoryImpl(get<AppDb>().tagDao(), get(), get()) as TagRepository }
  factory {
    TagAssignmentRepositoryImpl(
      get<AppDb>().tagAssignmentDao(),
      get()
    ) as TagAssignmentRepository
  }
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
  factory { ReminderGroupRepositoryImpl(get<AppDb>().reminderGroupDao()) as ReminderGroupRepository }
  factory {
    GoogleTaskListRepositoryImpl(
      get<AppDb>().googleTaskListsDao(),
      get()
    ) as GoogleTaskListRepository
  }
  factory { GoogleTaskRepositoryImpl(get<AppDb>().googleTasksDao(), get()) as GoogleTaskRepository }
  factory { NoteRepositoryImpl(get<AppDb>().notesDao(), get()) as NoteRepository }
  factory { ReminderRepositoryImpl(get<AppDb>().reminderDao()) as ReminderRepository }
  factory { ReminderV2RepositoryImpl(get<AppDb>().reminderV2Dao(), get()) as ReminderV2Repository }
  factory { ReminderV2BackfillUseCase(get(), get()) }
  factory { GroupV2RepositoryImpl(get<AppDb>().groupV2Dao(), get()) as GroupV2Repository }
  factory { GroupV2BackfillUseCase(get(), get()) }
  factory { RemoteFileMetadataRepositoryImpl(get<AppDb>().remoteFileMetadataDao(), get()) as RemoteFileMetadataRepository }
  factory { EventOccurrenceRepositoryImpl(get<AppDb>().eventOccurrenceDao(), get()) as EventOccurrenceRepository }
  factory { EventHistoryRepositoryImpl(get<AppDb>().eventHistoryDao(), get()) as EventHistoryRepository }
  factory { WorkflowRuleRepositoryImpl(get<AppDb>().workflowRuleDao(), get()) as WorkflowRuleRepository }
  factory { WorkflowTemplateRepositoryImpl(get<AppDb>().workflowTemplateDao(), get()) as WorkflowTemplateRepository }
  factory { HolidayRepositoryImpl(get<AppDb>().holidayDao(), get()) as HolidayRepository }
}
