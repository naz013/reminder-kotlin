package com.github.naz013.feature.settings

import android.content.Context
import com.github.naz013.feature.settings.calendar.CalendarSettingsViewModel
import com.github.naz013.feature.settings.calendar.country.HolidayCountryPickerResultHolder
import com.github.naz013.feature.settings.calendar.country.HolidayCountryViewModel
import com.github.naz013.feature.settings.calendar.usecase.CalculateGoogleCalendarEventOccurrencesUseCase
import com.github.naz013.feature.settings.calendar.usecase.ScanGoogleCalendarForNewEventsUseCase
import com.github.naz013.feature.settings.calendar.work.ScanGoogleCalendarEventsTask
import com.github.naz013.feature.settings.debug.DeveloperViewModel
import com.github.naz013.feature.settings.debug.ObjectExportViewModel
import com.github.naz013.feature.settings.debug.PopulateCalendarDemoDataUseCase
import com.github.naz013.feature.settings.digest.DigestSettingsViewModel
import com.github.naz013.feature.settings.export.CloudBackupSettingsViewModel
import com.github.naz013.feature.settings.export.DropboxLogin
import com.github.naz013.feature.settings.export.services.CloudServicesViewModel
import com.github.naz013.feature.settings.export.work.BackupSettingsTask
import com.github.naz013.feature.settings.export.work.ObservableBackupTask
import com.github.naz013.feature.settings.export.work.ObservableEraseDataTask
import com.github.naz013.feature.settings.export.work.ObservableSyncTask
import com.github.naz013.feature.settings.general.GeneralSettingsViewModel
import com.github.naz013.feature.settings.headeritems.HeaderItemsSettingsViewModel
import com.github.naz013.feature.settings.location.LocationSettingsViewModel
import com.github.naz013.feature.settings.location.MapStyleViewModel
import com.github.naz013.feature.settings.other.OtherSettingsViewModel
import com.github.naz013.feature.settings.other.PrivacyPolicyViewModel
import com.github.naz013.feature.settings.other.TermsViewModel
import com.github.naz013.feature.settings.other.whatsnew.WhatsNewViewModel
import com.github.naz013.feature.settings.proversion.ProVersionViewModel
import com.github.naz013.feature.settings.security.AddPinViewModel
import com.github.naz013.feature.settings.security.ChangePinViewModel
import com.github.naz013.feature.settings.security.DisablePinViewModel
import com.github.naz013.feature.settings.security.SecuritySettingsViewModel
import com.github.naz013.feature.settings.troubleshooting.TroubleshootingViewModel
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val featureSettingsModule = module {
  viewModelOf(::GeneralSettingsViewModel)
  viewModelOf(::HeaderItemsSettingsViewModel)
  viewModelOf(::ProVersionViewModel)
  viewModelOf(::TroubleshootingViewModel)
  viewModelOf(::OtherSettingsViewModel)
  viewModelOf(::PrivacyPolicyViewModel)
  viewModelOf(::TermsViewModel)
  viewModelOf(::WhatsNewViewModel)
  viewModelOf(::SecuritySettingsViewModel)
  viewModelOf(::AddPinViewModel)
  viewModelOf(::ChangePinViewModel)
  viewModelOf(::DisablePinViewModel)
  viewModelOf(::LocationSettingsViewModel)
  viewModelOf(::MapStyleViewModel)

  factoryOf(::ScanGoogleCalendarForNewEventsUseCase)
  factoryOf(::CalculateGoogleCalendarEventOccurrencesUseCase)

  factory<BackgroundTask>(named(ScanGoogleCalendarEventsTask.TASK_KEY)) {
    ScanGoogleCalendarEventsTask(
      get(),
      get()
    )
  }

  viewModelOf(::CalendarSettingsViewModel)
  viewModelOf(::HolidayCountryViewModel)
  single { HolidayCountryPickerResultHolder() }

  viewModelOf(::DigestSettingsViewModel)

  factory<BackgroundTask>(named(BackupSettingsTask.TASK_KEY)) { BackupSettingsTask(get()) }
  factory<BackgroundTask>(named(ObservableBackupTask.TASK_KEY)) { ObservableBackupTask(get()) }
  factory<BackgroundTask>(named(ObservableSyncTask.TASK_KEY)) { ObservableSyncTask(get()) }
  factory<BackgroundTask>(named(ObservableEraseDataTask.TASK_KEY)) {
    ObservableEraseDataTask(
      get(),
      get()
    )
  }

  viewModelOf(::CloudBackupSettingsViewModel)
  viewModelOf(::CloudServicesViewModel)

  viewModelOf(::SettingsHubViewModel)
  viewModelOf(::NoteSettingsViewModel)
  factory {
    PopulateCalendarDemoDataUseCase(
      reminderV2Repository = get(),
      birthdayRepository = get(),
      eventOccurrenceRepository = get(),
      groupV2Repository = get(),
      dateTimeManager = get(),
    )
  }
  viewModel {
    DeveloperViewModel(
      legalDocumentRepository = get(),
      prefs = get(),
      dispatcherProvider = get(),
      birthdayRepository = get(),
      dateTimeManager = get(),
      calendarEventRepository = get(),
      eventHistoryRepository = get(),
      eventOccurrenceRepository = get(),
      googleTaskListRepository = get(),
      googleTaskRepository = get(),
      noteRepository = get(),
      placeRepository = get(),
      recentQueryRepository = get(),
      recurPresetRepository = get(),
      remoteFileMetadataRepository = get(),
      usedTimeRepository = get(),
      buildInfo = get(),
      reminderV2Repository = get(),
      groupV2Repository = get(),
      workflowRuleRepository = get(),
      workflowTemplateRepository = get(),
      tagRepository = get(),
      tagAssignmentRepository = get(),
      activateReminderUseCase = get(),
      holidayRepository = get(),
      populateCalendarDemoDataUseCase = get(),
      routineRepository = get(),
      routineExecutionRepository = get(),
      insertDemoDataUseCase = get(),
      googleCalendarEventRepository = get(),
    )
  }
  viewModelOf(::ObjectExportViewModel)

  factory { (context: Context) ->
    DropboxLogin(context, get(), get(), get(), get(), get())
  }
}
