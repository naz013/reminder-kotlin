package com.elementary.tasks.core.utils

import android.app.Activity
import com.elementary.tasks.core.analytics.AnalyticsStateProviderImpl
import com.elementary.tasks.core.analytics.ReminderAnalyticsTracker
import com.elementary.tasks.core.apps.SelectApplicationViewModel
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.cloud.CloudKeysStorageImpl
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GoogleLogin
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
import com.elementary.tasks.core.cloud.repositories.BirthdayDataFlowRepository
import com.elementary.tasks.core.cloud.repositories.GroupDataFlowRepository
import com.elementary.tasks.core.cloud.repositories.NoteDataFlowRepository
import com.elementary.tasks.core.cloud.repositories.PlaceDataFlowRepository
import com.elementary.tasks.core.cloud.repositories.ReminderDataFlowRepository
import com.elementary.tasks.core.cloud.repositories.RepositoryManager
import com.elementary.tasks.core.cloud.repositories.SettingsDataFlowRepository
import com.elementary.tasks.core.cloud.storages.StorageManager
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.repository.NoteImageMigration
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.io.CacheUtil
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.io.UriReader
import com.elementary.tasks.core.utils.params.AppWidgetPreferencesImpl
import com.elementary.tasks.core.utils.params.AuthPreferencesImpl
import com.elementary.tasks.core.utils.params.DateTimePreferencesImpl
import com.elementary.tasks.core.utils.params.LocalePreferencesImpl
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.elementary.tasks.core.utils.params.ThemePreferencesImpl
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.work.WorkManagerProvider
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.googletasks.work.SaveNewTaskWorker
import com.elementary.tasks.googletasks.work.UpdateTaskWorker
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.groups.create.CreateGroupViewModel
import com.elementary.tasks.groups.list.GroupsViewModel
import com.elementary.tasks.groups.work.GroupDeleteBackupWorker
import com.elementary.tasks.groups.work.GroupSingleBackupWorker
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.notes.create.images.ImageDecoder
import com.elementary.tasks.places.create.PlaceViewModel
import com.elementary.tasks.places.list.PlacesViewModel
import com.elementary.tasks.places.work.PlaceDeleteBackupWorker
import com.elementary.tasks.places.work.PlaceSingleBackupWorker
import com.elementary.tasks.reminder.create.ReminderStateViewModel
import com.elementary.tasks.reminder.create.fragments.timer.UsedTimeViewModel
import com.elementary.tasks.reminder.work.CheckEventsWorker
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.elementary.tasks.settings.calendar.EventsImportViewModel
import com.elementary.tasks.settings.export.CloudViewModel
import com.elementary.tasks.settings.troubleshooting.TroubleshootingViewModel
import com.elementary.tasks.splash.SplashViewModel
import com.github.naz013.analytics.AnalyticsStateProvider
import com.github.naz013.analytics.initializeAnalytics
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.cloudapi.CloudKeysStorage
import com.github.naz013.common.datetime.DateTimePreferences
import com.github.naz013.ui.common.locale.LocalePreferences
import com.github.naz013.ui.common.login.AuthPreferences
import com.github.naz013.ui.common.theme.ThemePreferences
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {
  worker { SaveNewTaskWorker(get(), get(), get(), get(), get()) }
  worker { UpdateTaskWorker(get(), get(), get(), get(), get()) }
  worker { GroupDeleteBackupWorker(get(), get(), get(), get()) }
  worker { GroupSingleBackupWorker(get(), get(), get(), get()) }
  worker { PlaceDeleteBackupWorker(get(), get(), get(), get()) }
  worker { PlaceSingleBackupWorker(get(), get(), get(), get()) }
  worker { ReminderDeleteBackupWorker(get(), get(), get(), get()) }
  worker { ReminderSingleBackupWorker(get(), get(), get(), get()) }
  worker { CheckEventsWorker(get(), get(), get(), get(), get()) }
}

val viewModelModule = module {
  viewModel { (id: String) -> PlaceViewModel(id, get(), get(), get(), get(), get(), get(), get()) }

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
  viewModel { GroupsViewModel(get(), get(), get(), get(), get()) }

  viewModel { SelectApplicationViewModel(get(), get()) }
  viewModel { PlacesViewModel(get(), get(), get(), get(), get()) }
  viewModel { UsedTimeViewModel(get(), get(), get(), get()) }

  viewModel { CloudViewModel(get(), get(), get(), get(), get()) }
  viewModel { ReminderStateViewModel(get(), get()) }

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

  viewModel { TroubleshootingViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { EventsImportViewModel(get(), get(), get(), get(), get()) }
}

val converterModule = module {
  factory { BirthdayConverter(get()) }
  factory { GroupConverter(get()) }
  factory { NoteConverter(get(), get()) }
  factory { PlaceConverter(get()) }
  factory { ReminderConverter(get()) }
  factory { SettingsConverter(get()) }
  factory { ConverterManager(get(), get(), get(), get(), get(), get()) }
}

val completableModule = module {
  factory { ReminderCompletable(get(), get(), get(), get(), get()) }
  factory { ReminderDeleteCompletable(get()) }
  factory { CompletableManager(get(), get()) }
}

val storageModule = module {
  factory { CloudKeysStorageImpl(get()) as CloudKeysStorage }
  factory { StorageManager(get(), get(), get(), get()) }
}

val dataFlowRepositoryModule = module {
  factory { BirthdayDataFlowRepository(get()) }
  factory { GroupDataFlowRepository(get()) }
  factory { NoteDataFlowRepository(get(), get()) }
  factory { PlaceDataFlowRepository(get()) }
  factory { ReminderDataFlowRepository(get()) }
  factory { SettingsDataFlowRepository(get()) }
  factory { RepositoryManager(get(), get(), get(), get(), get(), get()) }
}

val utilModule = module {
  single { Prefs(get()) }
  factory { PresetInitProcessor(get(), get(), get(), get(), get(), get()) }
  single { ReminderExplanationVisibility(get()) }
  single { MemoryUtil() }
  factory { UriReader(get()) }
  single { BackupTool(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
  factory { GoogleCalendarUtils(get(), get(), get(), get()) }
  single { CacheUtil(get(), get()) }

  factory { SyncManagers(get(), get(), get(), get()) }

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
      get(),
      get()
    )
  }

  factory { RecurEventManager(get()) }

  single { RemotePrefs(get(), get(), get(), get()) }
  single { ThemePreferencesImpl(get()) as ThemePreferences }
  single { LocalePreferencesImpl(get()) as LocalePreferences }
  single { AuthPreferencesImpl(get()) as AuthPreferences }
  single { DateTimePreferencesImpl(get()) as DateTimePreferences }
  single { AppWidgetPreferencesImpl(get()) as AppWidgetPreferences }

  factory { Notifier(get(), get(), get(), get(), get(), get(), get()) }
  factory { JobScheduler(get(), get(), get(), get()) }

  factory { EnableThread(get(), get()) }
  factory { NoteImageMigration(get(), get()) }

  single { CurrentStateHolder(get(), get(), get(), get(), get()) }

  factory { WorkManagerProvider(get()) }
  factory { WorkerLauncher(get(), get()) }

  factory { AnalyticsStateProviderImpl(get()) as AnalyticsStateProvider }

  single { initializeAnalytics(get(), get()) }
  factory { ReminderAnalyticsTracker(get()) }

  single { FeatureManager(get()) }
  factory { GroupsUtil(get(), get(), get(), get()) }
  factory { ImageDecoder(get(), get(), get()) }

  factory { IdProvider() }

  factory { DateTimePickerProvider(get()) }
  factory { DoNotDisturbManager(get(), get()) }

  factory { (fragment: BaseNavigationFragment<*>, callback: GoogleLogin.LoginCallback) ->
    GoogleLogin(fragment, get(), get(), get(), get(), callback)
  }
  factory { (activity: Activity, callback: DropboxLogin.LoginCallback) ->
    DropboxLogin(activity, get(), get(), callback)
  }
  factory { (listener: LocationTracker.Listener) ->
    LocationTracker(listener, get(), get(), get())
  }
}
