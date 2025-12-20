package com.elementary.tasks.settings

import com.elementary.tasks.settings.birthday.work.CheckBirthdaysWorker
import com.elementary.tasks.settings.birthday.BirthdaySettingsViewModel
import com.elementary.tasks.settings.birthday.usecase.GetContactsWithMetadataUseCase
import com.elementary.tasks.settings.calendar.CalendarSettingsViewModel
import com.elementary.tasks.settings.calendar.usecase.ScanGoogleCalendarForNewEventsUseCase
import com.elementary.tasks.settings.calendar.work.ScanGoogleCalendarEventsWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
  factory { ScanGoogleCalendarForNewEventsUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
  factory { GetContactsWithMetadataUseCase(get()) }

  worker { ScanGoogleCalendarEventsWorker(get(), get(), get(), get()) }
  worker { CheckBirthdaysWorker(get(), get(), get(), get(), get(), get(), get()) }

  viewModel { CalendarSettingsViewModel(get(), get(), get(), get()) }
  viewModel { BirthdaySettingsViewModel(get()) }
}
