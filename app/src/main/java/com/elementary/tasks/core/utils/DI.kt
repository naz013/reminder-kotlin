package com.elementary.tasks.core.utils

import android.app.Activity
import com.elementary.tasks.core.analytics.AnalyticsStateProviderImpl
import com.elementary.tasks.core.analytics.ReminderAnalyticsTracker
import com.elementary.tasks.core.apps.SelectApplicationViewModel
import com.elementary.tasks.core.cloud.CloudKeysStorageImpl
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.data.repository.NoteImageMigration
import com.elementary.tasks.core.data.repository.ReminderSettingsRepositoryImpl
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.services.event.AutoBackupEventTask
import com.elementary.tasks.core.services.event.BirthdayEventTask
import com.elementary.tasks.core.services.event.BirthdayPermanentEventTask
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
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.elementary.tasks.core.utils.params.ThemePreferencesImpl
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.googletasks.work.SaveNewTaskTask
import com.elementary.tasks.googletasks.work.UpdateTaskTask
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.notes.create.drop.DroppedContentParser
import com.elementary.tasks.notes.create.images.ImageDecoder
import com.elementary.tasks.reminder.build.BuilderConfigureViewModel
import com.elementary.tasks.settings.other.PrivacyPolicyViewModel
import com.elementary.tasks.settings.other.TermsViewModel
import com.elementary.tasks.settings.other.whatsnew.WhatsNewViewModel
import com.elementary.tasks.settings.proversion.ProVersionViewModel
import com.elementary.tasks.settings.test.DeveloperViewModel
import com.elementary.tasks.settings.test.ObjectExportViewModel
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
import com.github.naz013.repository.ReminderSettingsRepository
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val workerModule =
  module {
    factory<BackgroundTask>(named(SaveNewTaskTask.TASK_KEY)) { SaveNewTaskTask(get(), get()) }
    factory<BackgroundTask>(named(UpdateTaskTask.TASK_KEY)) { UpdateTaskTask(get(), get()) }
  }

val viewModelModule =
  module {
    viewModelOf(::SelectApplicationViewModel)
    viewModelOf(::BuilderConfigureViewModel)
    viewModelOf(::ProVersionViewModel)

    viewModelOf(::SplashViewModel)

    viewModelOf(::TroubleshootingViewModel)

    viewModelOf(::PrivacyPolicyViewModel)
    viewModelOf(::TermsViewModel)
    viewModelOf(::WhatsNewViewModel)
    viewModelOf(::DeveloperViewModel)
    viewModelOf(::ObjectExportViewModel)
  }

val storageModule =
  module {
    factory { CloudKeysStorageImpl(get()) as CloudKeysStorage }
    factory { ReminderSettingsRepositoryImpl(get()) as ReminderSettingsRepository }
  }

val utilModule =
  module {
    factoryOf(::PresetInitProcessor)
    factoryOf(::UriReader)
    factoryOf(::GoogleCalendarUtils)

    singleOf(::MemoryUtil)
    singleOf(::BackupTool)
    singleOf(::CacheUtil)

    factoryOf(::RecurEventManager)

    singleOf(::Prefs)
    singleOf(::RemotePrefs)

    single { ThemePreferencesImpl(get()) as ThemePreferences }
    single { LocalePreferencesImpl(get()) as LocalePreferences }
    single { AuthPreferencesImpl(get()) as AuthPreferences }
    single { DateTimePreferencesImpl(get()) as DateTimePreferences }
    single { AppWidgetPreferencesImpl(get()) as AppWidgetPreferences }

    factory { Notifier(get(), get(), get(), get(), get(), get(), get()) }
    factory { JobScheduler(get(), get(), get(), get(), get(), get()) }

    factory { ActivateAllActiveRemindersUseCase(get(), get()) }
    factory { NoteImageMigration(get(), get()) }

    factory<BackgroundTask>(named(AutoBackupEventTask.TASK_KEY)) { AutoBackupEventTask(get(), get()) }
    factory<BackgroundTask>(named(BirthdayEventTask.TASK_KEY)) { BirthdayEventTask(get()) }
    factory<BackgroundTask>(named(BirthdayPermanentEventTask.TASK_KEY)) { BirthdayPermanentEventTask(get(), get()) }

    factory { AnalyticsStateProviderImpl(get()) as AnalyticsStateProvider }

    single { initializeAnalytics(get(), get()) }
    factory { ReminderAnalyticsTracker(get()) }

    factory { FeatureManager(get()) }
    factory { GroupsUtil(get(), get(), get(), get()) }
    factory { ImageDecoder(get(), get(), get()) }
    factory { DroppedContentParser(get()) }

    factory { DateTimePickerProvider(get()) }
    factory { DoNotDisturbManager(get(), get()) }

    factory { (activity: Activity, callback: DropboxLogin.LoginCallback) ->
      DropboxLogin(activity, get(), get(), callback, get())
    }
    factory { (listener: LocationTracker.Listener) ->
      LocationTracker(listener, get(), get(), get())
    }
  }
