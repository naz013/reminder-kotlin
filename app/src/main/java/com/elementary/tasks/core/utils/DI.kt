package com.elementary.tasks.core.utils

import android.app.Activity
import com.elementary.tasks.core.analytics.AnalyticsStateProviderImpl
import com.elementary.tasks.core.analytics.ReminderAnalyticsTracker
import com.elementary.tasks.core.apps.SelectApplicationViewModel
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.cloud.CloudKeysStorageImpl
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GoogleLogin
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
import com.elementary.tasks.googletasks.work.SaveNewTaskWorker
import com.elementary.tasks.googletasks.work.UpdateTaskWorker
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.notes.create.images.ImageDecoder
import com.elementary.tasks.reminder.work.CheckEventsWorker
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
  worker { CheckEventsWorker(get(), get(), get(), get(), get()) }
}

val viewModelModule = module {

  viewModel { SelectApplicationViewModel(get(), get()) }

  viewModel { CloudViewModel(get(), get(), get(), get(), get()) }

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
      get(),
      get()
    )
  }

  viewModel { TroubleshootingViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { EventsImportViewModel(get(), get(), get(), get(), get()) }
}

val storageModule = module {
  factory { CloudKeysStorageImpl(get()) as CloudKeysStorage }
}

val utilModule = module {
  factory { PresetInitProcessor(get(), get(), get(), get(), get(), get()) }
  single { ReminderExplanationVisibility(get()) }
  single { MemoryUtil() }
  factory { UriReader(get()) }
  single { BackupTool(get(), get()) }
  factory { GoogleCalendarUtils(get(), get(), get(), get()) }
  single { CacheUtil(get(), get()) }

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

  single { Prefs(get()) }
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
    GoogleLogin(fragment, get(), get(), get(), get(), callback, get())
  }
  factory { (activity: Activity, callback: DropboxLogin.LoginCallback) ->
    DropboxLogin(activity, get(), get(), callback, get())
  }
  factory { (listener: LocationTracker.Listener) ->
    LocationTracker(listener, get(), get(), get())
  }
}
