package com.elementary.tasks.settings

import com.elementary.tasks.settings.birthday.BirthdaySettingsViewModel
import com.elementary.tasks.settings.birthday.usecase.GetContactsWithMetadataUseCase
import com.elementary.tasks.settings.birthday.work.CheckBirthdaysTask
import com.elementary.tasks.settings.calendar.CalendarSettingsViewModel
import com.elementary.tasks.settings.calendar.country.HolidayCountryPickerResultHolder
import com.elementary.tasks.settings.calendar.country.HolidayCountryViewModel
import com.elementary.tasks.settings.calendar.usecase.ScanGoogleCalendarForNewEventsUseCase
import com.elementary.tasks.settings.calendar.work.ScanGoogleCalendarEventsTask
import com.elementary.tasks.settings.general.GeneralSettingsViewModel
import com.elementary.tasks.settings.location.LocationSettingsViewModel
import com.elementary.tasks.settings.location.MapStyleViewModel
import com.elementary.tasks.settings.other.OtherSettingsViewModel
import com.elementary.tasks.settings.reminders.RemindersSettingsViewModel
import com.elementary.tasks.settings.security.AddPinViewModel
import com.elementary.tasks.settings.security.ChangePinViewModel
import com.elementary.tasks.settings.security.DisablePinViewModel
import com.elementary.tasks.settings.security.SecuritySettingsViewModel
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val settingsModule = module {
  factoryOf(::ScanGoogleCalendarForNewEventsUseCase)
  factoryOf(::GetContactsWithMetadataUseCase)

  factory<BackgroundTask>(named(ScanGoogleCalendarEventsTask.TASK_KEY)) {
    ScanGoogleCalendarEventsTask(
      get(),
      get()
    )
  }
  factory<BackgroundTask>(named(CheckBirthdaysTask.TASK_KEY)) {
    CheckBirthdaysTask(
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }

  viewModelOf(::CalendarSettingsViewModel)
  viewModelOf(::HolidayCountryViewModel)
  single { HolidayCountryPickerResultHolder() }
  viewModelOf(::BirthdaySettingsViewModel)
  viewModelOf(::GeneralSettingsViewModel)
  viewModelOf(::NoteSettingsViewModel)
  viewModelOf(::SecuritySettingsViewModel)
  viewModelOf(::AddPinViewModel)
  viewModelOf(::ChangePinViewModel)
  viewModelOf(::DisablePinViewModel)
  viewModelOf(::LocationSettingsViewModel)
  viewModelOf(::MapStyleViewModel)
  viewModelOf(::OtherSettingsViewModel)
  viewModelOf(::RemindersSettingsViewModel)
  viewModelOf(::SettingsHubViewModel)
}
