package com.elementary.tasks.core.utils

import com.backdoor.engine.Recognizer
import com.elementary.tasks.birthdays.list.BirthdayModelAdapter
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.birthdays.work.CheckBirthdaysWorker
import com.elementary.tasks.birthdays.work.ScanContactsWorker
import com.elementary.tasks.birthdays.work.SingleBackupWorker
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.app_widgets.WidgetDataProvider
import com.elementary.tasks.core.apps.SelectApplicationViewModel
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.arch.LoginStateViewModel
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.completables.CompletableManager
import com.elementary.tasks.core.cloud.completables.ReminderCompletable
import com.elementary.tasks.core.cloud.completables.ReminderDeleteCompletable
import com.elementary.tasks.core.cloud.converters.BirthdayConverter
import com.elementary.tasks.core.cloud.converters.ConverterManager
import com.elementary.tasks.core.cloud.converters.GroupConverter
import com.elementary.tasks.core.cloud.converters.NoteConverter
import com.elementary.tasks.core.cloud.converters.PlaceConverter
import com.elementary.tasks.core.cloud.converters.ReminderConverter
import com.elementary.tasks.core.cloud.converters.SettingsConverter
import com.elementary.tasks.core.cloud.converters.TemplateConverter
import com.elementary.tasks.core.cloud.repositories.BirthdayRepository
import com.elementary.tasks.core.cloud.repositories.GroupRepository
import com.elementary.tasks.core.cloud.repositories.NoteRepository
import com.elementary.tasks.core.cloud.repositories.PlaceRepository
import com.elementary.tasks.core.cloud.repositories.ReminderRepository
import com.elementary.tasks.core.cloud.repositories.RepositoryManager
import com.elementary.tasks.core.cloud.repositories.SettingsRepository
import com.elementary.tasks.core.cloud.repositories.TemplateRepository
import com.elementary.tasks.core.cloud.storages.Dropbox
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.cloud.storages.LocalStorage
import com.elementary.tasks.core.cloud.storages.StorageManager
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.adapter.UiReminderPlaceAdapter
import com.elementary.tasks.core.data.adapter.UiReminderPreviewAdapter
import com.elementary.tasks.core.dialogs.VoiceHelpViewModel
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.core.view_models.birthdays.BirthdayViewModel
import com.elementary.tasks.core.view_models.birthdays.BirthdaysViewModel
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel
import com.elementary.tasks.core.view_models.day_view.DayViewViewModel
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskListViewModel
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskListsViewModel
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskViewModel
import com.elementary.tasks.core.view_models.groups.GroupViewModel
import com.elementary.tasks.core.view_models.groups.GroupsViewModel
import com.elementary.tasks.core.view_models.missed_calls.MissedCallViewModel
import com.elementary.tasks.core.view_models.month_view.MonthViewViewModel
import com.elementary.tasks.core.view_models.notes.NoteViewModel
import com.elementary.tasks.core.view_models.notes.NotesViewModel
import com.elementary.tasks.core.view_models.places.PlaceViewModel
import com.elementary.tasks.core.view_models.places.PlacesViewModel
import com.elementary.tasks.core.view_models.reminders.ActiveGpsRemindersViewModel
import com.elementary.tasks.core.view_models.reminders.ActiveRemindersViewModel
import com.elementary.tasks.core.view_models.reminders.ArchiveRemindersViewModel
import com.elementary.tasks.core.view_models.reminders.ReminderPreviewViewModel
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel
import com.elementary.tasks.core.view_models.sms_templates.SmsTemplateViewModel
import com.elementary.tasks.core.view_models.sms_templates.SmsTemplatesViewModel
import com.elementary.tasks.core.view_models.used_time.UsedTimeViewModel
import com.elementary.tasks.core.work.BackupDataWorker
import com.elementary.tasks.core.work.BackupSettingsWorker
import com.elementary.tasks.core.work.BackupWorker
import com.elementary.tasks.core.work.DeleteFileWorker
import com.elementary.tasks.core.work.ExportAllDataWorker
import com.elementary.tasks.core.work.LoadFileWorker
import com.elementary.tasks.core.work.SyncDataWorker
import com.elementary.tasks.core.work.SyncWorker
import com.elementary.tasks.day_view.DayViewProvider
import com.elementary.tasks.google_tasks.create.GoogleTasksStateViewModel
import com.elementary.tasks.google_tasks.work.SaveNewTaskWorker
import com.elementary.tasks.google_tasks.work.UpdateTaskWorker
import com.elementary.tasks.groups.work.GroupDeleteBackupWorker
import com.elementary.tasks.groups.work.GroupSingleBackupWorker
import com.elementary.tasks.home.HomeViewModel
import com.elementary.tasks.notes.create.CreateNoteViewModel
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.notes.work.NoteSingleBackupWorker
import com.elementary.tasks.places.create.CreatePlaceViewModel
import com.elementary.tasks.places.work.PlaceDeleteBackupWorker
import com.elementary.tasks.places.work.PlaceSingleBackupWorker
import com.elementary.tasks.reminder.create.ReminderStateViewModel
import com.elementary.tasks.reminder.work.CheckEventsWorker
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.elementary.tasks.settings.additional.work.TemplateDeleteBackupWorker
import com.elementary.tasks.settings.additional.work.TemplateSingleBackupWorker
import com.elementary.tasks.settings.export.CloudViewModel
import com.elementary.tasks.settings.voice.TimesViewModel
import com.elementary.tasks.splash.SplashViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {
  worker { BirthdayDeleteBackupWorker(get(), get(), get()) }
  worker { BackupDataWorker(get(), get(), get()) }
  worker { CheckBirthdaysWorker(get(), get(), get()) }
  worker { LoadFileWorker(get(), get(), get()) }
  worker { DeleteFileWorker(get(), get(), get()) }
  worker { BackupSettingsWorker(get(), get(), get()) }
  worker { SyncDataWorker(get(), get(), get(), get(), get()) }
  worker { SaveNewTaskWorker(get(), get(), get()) }
  worker { UpdateTaskWorker(get(), get(), get()) }
  worker { GroupDeleteBackupWorker(get(), get(), get()) }
  worker { GroupSingleBackupWorker(get(), get(), get()) }
  worker { DeleteNoteBackupWorker(get(), get(), get()) }
  worker { NoteSingleBackupWorker(get(), get(), get()) }
  worker { PlaceDeleteBackupWorker(get(), get(), get()) }
  worker { PlaceSingleBackupWorker(get(), get(), get()) }
  worker { ReminderDeleteBackupWorker(get(), get(), get()) }
  worker { ReminderSingleBackupWorker(get(), get(), get()) }
  worker { TemplateSingleBackupWorker(get(), get(), get()) }
  worker { TemplateDeleteBackupWorker(get(), get(), get()) }
  worker { CheckEventsWorker(get(), get(), get(), get(), get(), get()) }
  worker { SingleBackupWorker(get(), get(), get(), get()) }
}

val viewModelModule = module {
  viewModel { (id: String) -> BirthdayViewModel(id, get(), get(), get(), get(), get()) }
  viewModel { (id: String) -> ReminderViewModel(id, get(), get(), get(), get(), get(), get()) }
  viewModel { (id: String) ->
    ReminderPreviewViewModel(
      id,
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }
  viewModel { (id: String) -> SmsTemplateViewModel(id, get(), get(), get(), get()) }
  viewModel { (id: String) -> PlaceViewModel(id, get(), get(), get(), get()) }
  viewModel { (id: String) -> NoteViewModel(id, get(), get(), get(), get(), get(), get()) }
  viewModel { (id: String) -> GroupViewModel(id, get(), get(), get(), get()) }
  viewModel { (number: String) -> MissedCallViewModel(number, get(), get(), get(), get()) }
  viewModel { (listId: String) ->
    GoogleTaskListViewModel(
      listId,
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }
  viewModel { (id: String) ->
    GoogleTaskViewModel(
      id,
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }
  viewModel { (calculateFuture: Boolean) ->
    DayViewViewModel(calculateFuture, get(), get(), get(), get(), get(), get())
  }
  viewModel { (addReminders: Boolean, calculateFuture: Boolean) ->
    MonthViewViewModel(addReminders, calculateFuture, get(), get(), get(), get(), get())
  }
  viewModel { BirthdaysViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { SmsTemplatesViewModel(get(), get(), get(), get()) }
  viewModel { ConversationViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
  viewModel { SelectApplicationViewModel() }
  viewModel { PlacesViewModel(get(), get(), get(), get(), get()) }
  viewModel { UsedTimeViewModel(get(), get(), get(), get()) }
  viewModel { ActiveGpsRemindersViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { ActiveRemindersViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { ArchiveRemindersViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { NotesViewModel(get(), get(), get(), get(), get(), get(), get()) }
  viewModel { GoogleTaskListsViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get()) }
  viewModel { GroupsViewModel(get(), get(), get(), get()) }
  viewModel { CloudViewModel(get(), get()) }
  viewModel { ReminderStateViewModel() }
  viewModel { GoogleTasksStateViewModel() }
  viewModel { CreateNoteViewModel() }
  viewModel { CreatePlaceViewModel() }
  viewModel { TimesViewModel() }
  viewModel { LoginStateViewModel() }
  viewModel { SplashViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { VoiceHelpViewModel(get(), get()) }
}

val converterModule = module {
  single { BirthdayConverter() }
  single { GroupConverter() }
  single { NoteConverter() }
  single { PlaceConverter() }
  single { ReminderConverter() }
  single { SettingsConverter() }
  single { TemplateConverter() }
  single { ConverterManager(get(), get(), get(), get(), get(), get(), get()) }
}

val completableModule = module {
  single { ReminderCompletable(get(), get()) }
  single { ReminderDeleteCompletable(get()) }
  single { CompletableManager(get(), get()) }
}

val storageModule = module {
  single { Dropbox(get()) }
  single { GDrive(get(), get()) }
  single { LocalStorage(get()) }
  single { StorageManager(get(), get(), get(), get()) }
}

val repositoryModule = module {
  single { BirthdayRepository(get()) }
  single { GroupRepository(get()) }
  single { NoteRepository(get()) }
  single { PlaceRepository(get()) }
  single { ReminderRepository(get()) }
  single { SettingsRepository(get()) }
  single { TemplateRepository(get()) }
  single { RepositoryManager(get(), get(), get(), get(), get(), get(), get()) }
}

val utilModule = module {
  single { AppDb.getAppDatabase(get()) }
  single { Prefs(get()) }
  single { GTasks(get(), get(), get()) }
  single { SoundStackHolder(get()) }
  single { ThemeProvider(get(), get()) }
  single { BackupTool(get(), get(), get()) }
  single { Dialogues(get()) }
  single { Language(get()) }
  single { CalendarUtils(get(), get(), get()) }
  single { providesRecognizer(get(), get()) }
  single { CacheUtil(get()) }
  single { GlobalButtonObservable() }
  single { ImagesSingleton() }
  single { SyncManagers(get(), get(), get(), get()) }
  single { EventControlFactory(get(), get(), get(), get()) }
  single { RemotePrefs(get(), get()) }

  factory { WidgetDataProvider(get()) }
  single { SyncWorker(get(), get(), get()) }
  single { BackupWorker(get(), get()) }
  single { ExportAllDataWorker(get()) }
  single { ScanContactsWorker(get(), get()) }
  factory { EnableThread(get(), get()) }
  single { CurrentStateHolder(get(), get(), get(), get()) }
  single { BirthdayModelAdapter(get()) }
  single { DayViewProvider(get(), get()) }

  single { DispatcherProvider() }

  single { WorkManagerProvider(get()) }

  single { AnalyticsEventSender(FirebaseAnalytics.getInstance(get())) }

  single { TextProvider(get()) }
}

val adapterModule = module {
  single { UiReminderPreviewAdapter(get(), get(), get()) }
  single { UiReminderPlaceAdapter() }
}

fun providesRecognizer(prefs: Prefs, language: Language) =
  Recognizer.Builder()
    .setLocale(language.getVoiceLanguage(prefs.voiceLocale))
    .setTimes(
      listOf(
        prefs.morningTime,
        prefs.noonTime,
        prefs.eveningTime,
        prefs.nightTime
      )
    )
    .build()