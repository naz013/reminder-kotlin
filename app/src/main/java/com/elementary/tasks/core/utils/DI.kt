package com.elementary.tasks.core.utils

import android.content.Context
import com.elementary.tasks.calendar.holidays.HolidaySettingsGateImpl
import com.elementary.tasks.core.apps.SelectApplicationViewModel
import com.elementary.tasks.core.cloud.CloudKeysStorageImpl
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.data.repository.ReminderSettingsRepositoryImpl
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.notes.AppNoteFontProvider
import com.elementary.tasks.core.notes.AppNoteNotifier
import com.elementary.tasks.core.notes.AppNotePreferences
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.services.event.AutoBackupEventTask
import com.elementary.tasks.core.services.event.BirthdayEventTask
import com.elementary.tasks.core.services.event.BirthdayPermanentEventTask
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.io.CacheUtil
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.github.naz013.ui.group.GroupsUtil
import com.elementary.tasks.navigation.BottomNavInitViewModel
import com.elementary.tasks.settings.other.PrivacyPolicyViewModel
import com.elementary.tasks.settings.other.TermsViewModel
import com.elementary.tasks.settings.other.whatsnew.WhatsNewViewModel
import com.elementary.tasks.settings.proversion.ProVersionViewModel
import com.elementary.tasks.settings.test.DeveloperViewModel
import com.elementary.tasks.settings.test.ObjectExportViewModel
import com.elementary.tasks.settings.troubleshooting.TroubleshootingViewModel
import com.github.naz013.cloudapi.CloudKeysStorage
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.holidaysapi.HolidaySettingsGate
import com.github.naz013.logic.reminder.RecurEventManager
import com.github.naz013.notification.NotificationApi
import com.github.naz013.repository.ReminderSettingsRepository
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.ui.note.NoteFontProvider
import com.github.naz013.ui.note.NoteNotifier
import com.github.naz013.ui.note.NotePreferences
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelModule = module {
  viewModelOf(::SelectApplicationViewModel)
  viewModelOf(::ProVersionViewModel)

  viewModelOf(::BottomNavInitViewModel)

  viewModelOf(::TroubleshootingViewModel)

  viewModelOf(::PrivacyPolicyViewModel)
  viewModelOf(::TermsViewModel)
  viewModelOf(::WhatsNewViewModel)
  viewModel {
    DeveloperViewModel(
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
      get(),
      get(),
      get(),
    )
  }
  viewModelOf(::ObjectExportViewModel)
}

val storageModule = module {
  factory { CloudKeysStorageImpl(get()) as CloudKeysStorage }
  factory { ReminderSettingsRepositoryImpl(get()) as ReminderSettingsRepository }
}

val utilModule = module {
  factoryOf(::PresetInitProcessor)
  factory { GoogleCalendarUtils(get(), get(), get(), get()) as GoogleCalendarApi }

  singleOf(::BackupTool)
  singleOf(::CacheUtil)

  factoryOf(::RecurEventManager)

  singleOf(::Prefs)
  singleOf(::RemotePrefs)

  factory { AppNotePreferences(get()) as NotePreferences }
  factory { AppNoteFontProvider() as NoteFontProvider }
  factory { AppNoteNotifier(get()) as NoteNotifier }

  factory { Notifier(get(), get(), get(), get(), get(), get(), get()) }
  factory { Notifier(get(), get(), get(), get(), get(), get(), get()) as NotificationApi }
  factory { JobScheduler(get(), get(), get(), get(), get(), get()) as JobSchedulerApi }

  factory { ActivateAllActiveRemindersUseCase(get(), get()) }

  factory<BackgroundTask>(named(AutoBackupEventTask.TASK_KEY)) { AutoBackupEventTask(get(), get()) }
  factory<BackgroundTask>(named(BirthdayEventTask.TASK_KEY)) { BirthdayEventTask(get()) }
  factory<BackgroundTask>(named(BirthdayPermanentEventTask.TASK_KEY)) { BirthdayPermanentEventTask(get(), get()) }

  factory { FeatureManager(get()) as FeatureFlags }
  factory { HolidaySettingsGateImpl(get(), get()) as HolidaySettingsGate }
  factory { GroupsUtil(get(), get(), get()) }

  factory { DoNotDisturbManager(get(), get()) }

  factory { (context: Context) ->
    DropboxLogin(context, get(), get(), get(), get(), get())
  }
  factory { (listener: LocationTracker.Listener) ->
    LocationTracker(listener, get(), get(), get())
  }
}
