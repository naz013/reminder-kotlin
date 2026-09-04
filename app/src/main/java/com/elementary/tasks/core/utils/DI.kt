package com.elementary.tasks.core.utils

import com.elementary.tasks.core.birthdays.AppBirthdayNotifier
import com.elementary.tasks.core.birthdays.AppBirthdayPreferences
import com.elementary.tasks.core.calendar.AppCalendarPreferences
import com.elementary.tasks.core.cloud.CloudKeysStorageImpl
import com.elementary.tasks.core.data.repository.ReminderSettingsRepositoryImpl
import com.elementary.tasks.core.digest.DigestSettingsGateImpl
import com.elementary.tasks.core.holidays.HolidaySettingsGateImpl
import com.elementary.tasks.core.home.AppHomePreferences
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.notes.AppNoteFontProvider
import com.elementary.tasks.core.notes.AppNoteNotifier
import com.elementary.tasks.core.notes.AppNotePreferences
import com.elementary.tasks.core.onboarding.AppOnboardingPreferences
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.services.event.AutoBackupEventTask
import com.elementary.tasks.core.services.event.BirthdayEventTask
import com.elementary.tasks.core.services.event.BirthdayPermanentEventTask
import com.elementary.tasks.core.utils.io.CacheUtil
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.elementary.tasks.navigation.BottomNavInitViewModel
import com.elementary.tasks.navigation.InAppAlertViewModel
import com.github.naz013.cloudapi.CloudKeysStorage
import com.github.naz013.feature.calendar.CalendarPreferences
import com.github.naz013.feature.home.HomePreferences
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.digestapi.DigestSettingsGate
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.holidaysapi.HolidaySettingsGate
import com.github.naz013.logic.birthday.BirthdayNotifier
import com.github.naz013.logic.birthday.BirthdayPreferences
import com.github.naz013.logic.reminder.RecurEventManager
import com.github.naz013.notification.NotificationApi
import com.github.naz013.onboarding.OnboardingPreferences
import com.github.naz013.repository.ReminderSettingsRepository
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.ui.note.NoteFontProvider
import com.github.naz013.ui.note.NoteNotifier
import com.github.naz013.ui.note.NotePreferences
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelModule = module {
  viewModelOf(::BottomNavInitViewModel)
  viewModelOf(::InAppAlertViewModel)
}

val storageModule = module {
  factory { CloudKeysStorageImpl(get()) as CloudKeysStorage }
  factory { ReminderSettingsRepositoryImpl(get()) as ReminderSettingsRepository }
}

val utilModule = module {
  factory { GoogleCalendarUtils(get(), get(), get(), get()) as GoogleCalendarApi }

  singleOf(::CacheUtil)

  factoryOf(::RecurEventManager)

  singleOf(::Prefs)
  singleOf(::RemotePrefs)

  factory { AppNotePreferences(get()) as NotePreferences }
  factory { AppNoteFontProvider(get()) as NoteFontProvider }
  factory { AppNoteNotifier(get()) as NoteNotifier }

  factory { AppBirthdayPreferences(get()) as BirthdayPreferences }
  factory { AppBirthdayNotifier(get()) as BirthdayNotifier }

  factory { Notifier(get(), get(), get(), get(), get(), get(), get()) }
  factory { Notifier(get(), get(), get(), get(), get(), get(), get()) as NotificationApi }
  factory { JobScheduler(get(), get(), get(), get(), get(), get()) as JobSchedulerApi }

  factory { ActivateAllActiveRemindersUseCase(get(), get()) }

  factory<BackgroundTask>(named(AutoBackupEventTask.TASK_KEY)) { AutoBackupEventTask(get(), get()) }
  factory<BackgroundTask>(named(BirthdayEventTask.TASK_KEY)) { BirthdayEventTask(get()) }
  factory<BackgroundTask>(named(BirthdayPermanentEventTask.TASK_KEY)) { BirthdayPermanentEventTask(get(), get()) }

  factory { FeatureManager(get()) as FeatureFlags }
  factory { HolidaySettingsGateImpl(get(), get()) as HolidaySettingsGate }
  factory { DigestSettingsGateImpl(get(), get()) as DigestSettingsGate }
  factory { AppCalendarPreferences(get()) as CalendarPreferences }
  factory { AppHomePreferences(get()) as HomePreferences }
  factory { AppOnboardingPreferences(get()) as OnboardingPreferences }

  factory { (listener: LocationTracker.Listener) ->
    LocationTracker(listener, get(), get(), get())
  }
}
