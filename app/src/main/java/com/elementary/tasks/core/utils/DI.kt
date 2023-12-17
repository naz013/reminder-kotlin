package com.elementary.tasks.core.utils

import android.app.Activity
import android.content.Context
import com.backdoor.engine.Recognizer
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.ReminderAnalyticsTracker
import com.elementary.tasks.core.analytics.VoiceAnalyticsTracker
import com.elementary.tasks.core.apps.SelectApplicationViewModel
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.appwidgets.WidgetDataProvider
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.arch.LoginStateViewModel
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.completables.CompletableManager
import com.elementary.tasks.core.cloud.completables.ReminderCompletable
import com.elementary.tasks.core.cloud.completables.ReminderDeleteCompletable
import com.elementary.tasks.core.cloud.converters.BirthdayConverter
import com.elementary.tasks.core.cloud.converters.ConverterManager
import com.elementary.tasks.core.cloud.converters.GroupConverter
import com.elementary.tasks.core.cloud.converters.NoteConverter
import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.elementary.tasks.core.cloud.converters.PlaceConverter
import com.elementary.tasks.core.cloud.converters.ReminderConverter
import com.elementary.tasks.core.cloud.converters.SettingsConverter
import com.elementary.tasks.core.cloud.repositories.BirthdayDataFlowRepository
import com.elementary.tasks.core.cloud.repositories.GroupDataFlowRepository
import com.elementary.tasks.core.cloud.repositories.NoteDataFlowRepository
import com.elementary.tasks.core.cloud.repositories.PlaceDataFlowRepository
import com.elementary.tasks.core.cloud.repositories.ReminderDataFlowRepository
import com.elementary.tasks.core.cloud.repositories.RepositoryManager
import com.elementary.tasks.core.cloud.repositories.SettingsDataFlowRepository
import com.elementary.tasks.core.cloud.storages.Dropbox
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.cloud.storages.LocalStorage
import com.elementary.tasks.core.cloud.storages.StorageManager
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.repository.NoteImageMigration
import com.elementary.tasks.core.dialogs.VoiceHelpViewModel
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.datetime.NowDateTimeProvider
import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceManager
import com.elementary.tasks.core.utils.datetime.recurrence.builder.RuleBuilder
import com.elementary.tasks.core.utils.datetime.recurrence.parser.TagParser
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.io.CacheUtil
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.io.UriReader
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.core.utils.ui.GlobalButtonObservable
import com.elementary.tasks.core.utils.work.WorkManagerProvider
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.work.BackupDataWorker
import com.elementary.tasks.core.work.BackupSettingsWorker
import com.elementary.tasks.core.work.BackupWorker
import com.elementary.tasks.core.work.DeleteFileWorker
import com.elementary.tasks.core.work.ExportAllDataWorker
import com.elementary.tasks.core.work.LoadFileWorker
import com.elementary.tasks.core.work.SyncDataWorker
import com.elementary.tasks.core.work.SyncWorker
import com.elementary.tasks.googletasks.GoogleTasksViewModel
import com.elementary.tasks.googletasks.list.TaskListViewModel
import com.elementary.tasks.googletasks.preview.GoogleTaskPreviewViewModel
import com.elementary.tasks.googletasks.task.GoogleTaskViewModel
import com.elementary.tasks.googletasks.tasklist.GoogleTaskListViewModel
import com.elementary.tasks.googletasks.work.SaveNewTaskWorker
import com.elementary.tasks.googletasks.work.UpdateTaskWorker
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.groups.create.CreateGroupViewModel
import com.elementary.tasks.groups.list.GroupsViewModel
import com.elementary.tasks.groups.work.GroupDeleteBackupWorker
import com.elementary.tasks.groups.work.GroupSingleBackupWorker
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.notes.create.CreateNoteViewModel
import com.elementary.tasks.notes.create.images.ImageDecoder
import com.elementary.tasks.notes.list.NotesViewModel
import com.elementary.tasks.notes.list.archived.ArchivedNotesViewModel
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.notes.preview.NotePreviewViewModel
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.notes.work.NoteSingleBackupWorker
import com.elementary.tasks.places.create.PlaceViewModel
import com.elementary.tasks.places.list.PlacesViewModel
import com.elementary.tasks.places.work.PlaceDeleteBackupWorker
import com.elementary.tasks.places.work.PlaceSingleBackupWorker
import com.elementary.tasks.reminder.create.EditReminderViewModel
import com.elementary.tasks.reminder.create.ReminderStateViewModel
import com.elementary.tasks.reminder.create.fragments.timer.UsedTimeViewModel
import com.elementary.tasks.reminder.dialog.ReminderViewModel
import com.elementary.tasks.reminder.preview.FullScreenMapViewModel
import com.elementary.tasks.reminder.preview.ReminderPreviewViewModel
import com.elementary.tasks.reminder.work.CheckEventsWorker
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.elementary.tasks.settings.calendar.EventsImportViewModel
import com.elementary.tasks.settings.export.CloudViewModel
import com.elementary.tasks.settings.troubleshooting.TroubleshootingViewModel
import com.elementary.tasks.settings.voice.TimesViewModel
import com.elementary.tasks.splash.SplashViewModel
import com.elementary.tasks.voice.ConversationViewModel
import com.elementary.tasks.voice.VoiceResultDialogViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.Module
import org.koin.dsl.module
import org.threeten.bp.ZoneId

val workerModule = module {
  worker { BackupDataWorker(get(), get(), get(), get()) }
  worker { LoadFileWorker(get(), get(), get(), get()) }
  worker { DeleteFileWorker(get(), get(), get(), get()) }
  worker { BackupSettingsWorker(get(), get(), get(), get()) }
  worker { SyncDataWorker(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
  worker { SaveNewTaskWorker(get(), get(), get(), get()) }
  worker { UpdateTaskWorker(get(), get(), get(), get()) }
  worker { GroupDeleteBackupWorker(get(), get(), get(), get()) }
  worker { GroupSingleBackupWorker(get(), get(), get(), get()) }
  worker { DeleteNoteBackupWorker(get(), get(), get(), get()) }
  worker { NoteSingleBackupWorker(get(), get(), get(), get()) }
  worker { PlaceDeleteBackupWorker(get(), get(), get(), get()) }
  worker { PlaceSingleBackupWorker(get(), get(), get(), get()) }
  worker { ReminderDeleteBackupWorker(get(), get(), get(), get()) }
  worker { ReminderSingleBackupWorker(get(), get(), get(), get()) }
  worker { CheckEventsWorker(get(), get(), get(), get(), get()) }
}

val viewModelModule = module {
  viewModel { (id: String) -> ReminderViewModel(id, get(), get(), get(), get()) }
  viewModel { (id: String) -> VoiceResultDialogViewModel(id, get(), get(), get()) }
  viewModel { (id: String) -> FullScreenMapViewModel(id, get(), get()) }
  viewModel { (id: String) ->
    EditReminderViewModel(
      id,
      get(),
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
      get(),
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

  viewModel { (id: String) -> PlaceViewModel(id, get(), get(), get(), get(), get(), get()) }
  viewModel { (id: String) ->
    NotePreviewViewModel(
      id,
      get(),
      get(),
      get(),
      get(),
      get(),
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

  viewModel { (id: String) ->
    CreateGroupViewModel(
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
  viewModel { GroupsViewModel(get(), get(), get(), get()) }

  viewModel { (listId: String) ->
    GoogleTaskListViewModel(
      listId,
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
  viewModel { (id: String) ->
    GoogleTaskViewModel(
      id,
      get(),
      get(),
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
  viewModel { (id: String) ->
    GoogleTaskPreviewViewModel(
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
  viewModel { (listId: String) ->
    TaskListViewModel(
      listId,
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
  viewModel { GoogleTasksViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }

  viewModel {
    ConversationViewModel(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
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
  viewModel { SelectApplicationViewModel(get(), get()) }
  viewModel { PlacesViewModel(get(), get(), get(), get(), get()) }
  viewModel { UsedTimeViewModel(get(), get(), get()) }

  viewModel {
    NotesViewModel(
      get(),
      get(),
      get(),
      get(),
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
  viewModel {
    ArchivedNotesViewModel(
      get(),
      get(),
      get(),
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

  viewModel { CloudViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { ReminderStateViewModel(get(), get()) }

  viewModel {
    CreateNoteViewModel(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
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
  viewModel { TimesViewModel(get(), get()) }
  viewModel { LoginStateViewModel() }
  viewModel {
    SplashViewModel(
      get(),
      get(),
      get(),
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
  viewModel { VoiceHelpViewModel(get(), get()) }

  viewModel { TroubleshootingViewModel(get(), get(), get()) }
  viewModel { EventsImportViewModel(get(), get(), get(), get(), get()) }
}

val converterModule = module {
  single { BirthdayConverter(get(), get()) }
  single { GroupConverter(get()) }
  single { NoteConverter(get(), get()) }
  single { PlaceConverter(get()) }
  single { ReminderConverter(get(), get()) }
  single { SettingsConverter(get()) }
  single { ConverterManager(get(), get(), get(), get(), get(), get()) }
}

val completableModule = module {
  single { ReminderCompletable(get(), get(), get(), get(), get()) }
  single { ReminderDeleteCompletable(get()) }
  single { CompletableManager(get(), get()) }
}

val storageModule = module {
  single { Dropbox(get(), get()) }
  single { GDrive(get(), get(), get(), get()) }
  single { LocalStorage(get()) }
  single { StorageManager(get(), get(), get(), get(), get()) }
}

val dataFlowRepositoryModule = module {
  single { BirthdayDataFlowRepository(get()) }
  single { GroupDataFlowRepository(get()) }
  single { NoteDataFlowRepository(get(), get(), get()) }
  single { PlaceDataFlowRepository(get()) }
  single { ReminderDataFlowRepository(get()) }
  single { SettingsDataFlowRepository(get()) }
  single { RepositoryManager(get(), get(), get(), get(), get(), get()) }
}

fun dbModule(context: Context): Module {
  val appDb = AppDb.getAppDatabase(context)
  return module {
    single { appDb }
    single { appDb.birthdaysDao() }
    single { appDb.reminderDao() }
    single { appDb.reminderGroupDao() }
    single { appDb.googleTaskListsDao() }
    single { appDb.googleTasksDao() }
    single { appDb.calendarEventsDao() }
    single { appDb.notesDao() }
    single { appDb.placesDao() }
    single { appDb.usedTimeDao() }
    single { appDb.recurPresetDao() }
    single { appDb.recentQueryDao() }

    single { NoteToOldNoteConverter(get()) }
  }
}

val utilModule = module {
  single { Prefs(get()) }
  single { PresetInitProcessor(get(), get(), get()) }
  single { ReminderExplanationVisibility(get()) }
  single { GTasks(get(), get(), get(), get(), get(), get(), get()) }
  single { SoundStackHolder(get()) }
  single { ThemeProvider(get(), get()) }
  single { MemoryUtil(get()) }
  single { UriReader(get()) }
  single { BackupTool(get(), get(), get(), get(), get(), get(), get()) }
  single { Dialogues(get()) }
  single { Language(get(), get(), get()) }
  single { GoogleCalendarUtils(get(), get(), get(), get()) }
  factory { providesRecognizer(get(), get()) }
  single { CacheUtil(get()) }
  single { GlobalButtonObservable() }
  single { ImagesSingleton(get()) }
  single { SyncManagers(get(), get(), get(), get()) }
  single {
    EventControlFactory(
      get(),
      get(),
      get(),
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

  single { RuleBuilder() }
  single { TagParser() }

  single { RecurrenceManager(get(), get(), get()) }
  single { RecurEventManager(get()) }

  single { RemotePrefs(get(), get(), get(), get()) }

  single { Notifier(get(), get(), get(), get(), get()) }
  single { JobScheduler(get(), get(), get(), get()) }
  single { UpdatesHelper(get()) }

  factory { WidgetDataProvider(get(), get(), get(), get()) }

  single { SyncWorker(get(), get(), get(), get(), get(), get()) }
  single { BackupWorker(get(), get()) }
  single { ExportAllDataWorker(get()) }

  factory { EnableThread(get(), get()) }
  single { NoteImageMigration(get(), get()) }

  single { CurrentStateHolder(get(), get(), get(), get(), get()) }

  single { DispatcherProvider() }

  single { WorkManagerProvider(get()) }
  single { WorkerLauncher(get(), get()) }

  single { AnalyticsEventSender(FirebaseAnalytics.getInstance(get()), get()) }
  single { ReminderAnalyticsTracker(get()) }
  single { VoiceAnalyticsTracker(get()) }

  single { TextProvider(get()) }
  single { FeatureManager(get()) }
  single { GroupsUtil(get(), get(), get(), get()) }
  single { ImageDecoder(get(), get(), get()) }

  single { IdProvider() }

  single { DateTimeManager(get(), get(), get(), NowDateTimeProvider()) }
  single { DateTimePickerProvider(get()) }
  single { DoNotDisturbManager(get(), get()) }

  factory { (fragment: BaseNavigationFragment<*>, callback: GoogleLogin.LoginCallback) ->
    GoogleLogin(fragment, get(), get(), get(), callback)
  }
  factory { (activity: Activity, callback: DropboxLogin.LoginCallback) ->
    DropboxLogin(activity, get(), callback)
  }
  factory { (listener: LocationTracker.Listener) ->
    LocationTracker(listener, get(), get(), get())
  }
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
    .setTimeZone(ZoneId.systemDefault().id)
    .build()
    .apply {
      enableLogging = BuildConfig.DEBUG
    }
