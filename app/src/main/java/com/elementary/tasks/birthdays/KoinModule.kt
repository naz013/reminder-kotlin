package com.elementary.tasks.birthdays

import com.elementary.tasks.birthdays.create.EditBirthdayViewModel
import com.elementary.tasks.birthdays.create.UiBirthdayDateFormatter
import com.elementary.tasks.birthdays.dialog.ShowBirthdayViewModel
import com.elementary.tasks.birthdays.list.BirthdaysViewModel
import com.elementary.tasks.birthdays.preview.PreviewBirthdayViewModel
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.elementary.tasks.birthdays.work.CheckBirthdaysWorker
import com.elementary.tasks.birthdays.work.ScanContactsWorker
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
    )
  }
  viewModel { (id: String) ->
    EditBirthdayViewModel(
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
    )
  }
  viewModel { BirthdaysViewModel(get(), get(), get(), get()) }
  viewModel { BirthdaySettingsViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { PreviewBirthdayViewModel(get(), get(), get(), get(), get(), get()) }

  worker { CheckBirthdaysWorker(get(), get(), get(), get(), get(), get(), get()) }

  factory { ScanContactsWorker(get(), get(), get(), get(), get()) }

  factory { DeleteBirthdayUseCase(get(), get(), get(), get()) }
  factory { SaveBirthdayUseCase(get(), get(), get(), get()) }
}
