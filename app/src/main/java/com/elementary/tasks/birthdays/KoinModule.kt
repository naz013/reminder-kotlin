package com.elementary.tasks.birthdays

import com.elementary.tasks.birthdays.create.AddBirthdayViewModel
import com.elementary.tasks.birthdays.create.UiBirthdayDateFormatter
import com.elementary.tasks.birthdays.dialog.ShowBirthdayViewModel
import com.elementary.tasks.birthdays.list.BirthdaysViewModel
import com.elementary.tasks.birthdays.preview.BirthdayPreviewViewModel
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.birthdays.work.CheckBirthdaysWorker
import com.elementary.tasks.birthdays.work.ScanContactsWorker
import com.elementary.tasks.birthdays.work.SingleBackupWorker
import com.elementary.tasks.settings.birthday.BirthdaySettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val birthdaysModule = module {
  factory { UiBirthdayDateFormatter(get()) }

  viewModel { (id: String) ->
    ShowBirthdayViewModel(
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
  viewModel { (id: String) ->
    AddBirthdayViewModel(
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
      get()
    )
  }
  viewModel { BirthdaysViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { BirthdaySettingsViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { BirthdayPreviewViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }

  worker { BirthdayDeleteBackupWorker(get(), get(), get(), get()) }
  worker { CheckBirthdaysWorker(get(), get(), get(), get(), get(), get()) }
  worker { SingleBackupWorker(get(), get(), get(), get()) }

  factory { ScanContactsWorker(get(), get(), get(), get()) }
}
