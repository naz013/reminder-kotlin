package com.elementary.tasks.settings

import com.elementary.tasks.settings.birthday.BirthdaySettingsViewModel
import com.elementary.tasks.settings.birthday.usecase.GetContactsWithMetadataUseCase
import com.elementary.tasks.settings.birthday.work.CheckBirthdaysWorker
import com.elementary.tasks.settings.calendar.CalendarSettingsViewModel
import com.elementary.tasks.settings.calendar.usecase.ScanGoogleCalendarForNewEventsUseCase
import com.elementary.tasks.settings.calendar.work.ScanGoogleCalendarEventsWorker
import com.elementary.tasks.settings.general.GeneralSettingsViewModel
import com.elementary.tasks.settings.location.LocationSettingsViewModel
import com.elementary.tasks.settings.location.MapStyleViewModel
import com.elementary.tasks.settings.other.OtherSettingsViewModel
import com.elementary.tasks.settings.reminders.RemindersSettingsViewModel
import com.elementary.tasks.settings.security.SecuritySettingsViewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule =
  module {
    factory { ScanGoogleCalendarForNewEventsUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { GetContactsWithMetadataUseCase(get()) }

    worker { ScanGoogleCalendarEventsWorker(get(), get(), get(), get()) }
    worker { CheckBirthdaysWorker(get(), get(), get(), get(), get(), get(), get()) }

    viewModel { CalendarSettingsViewModel(get(), get(), get(), get()) }
    viewModel { BirthdaySettingsViewModel(get(), get(), get(), get(), get()) }
    viewModel { GeneralSettingsViewModel(get(), get()) }
    viewModel { NoteSettingsViewModel(get()) }
    viewModel { SecuritySettingsViewModel(get()) }
    viewModel { LocationSettingsViewModel(get(), get(), get()) }
    viewModel { MapStyleViewModel(get()) }
    viewModel { OtherSettingsViewModel(get(), get()) }
    viewModel { RemindersSettingsViewModel(get(), get(), get()) }
    viewModel { SettingsHubViewModel(get(), get(), get(), get()) }
  }
