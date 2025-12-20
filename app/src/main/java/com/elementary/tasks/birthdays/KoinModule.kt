package com.elementary.tasks.birthdays

import com.elementary.tasks.birthdays.actions.GetBirthdayActionsUseCase
import com.elementary.tasks.birthdays.create.EditBirthdayViewModel
import com.elementary.tasks.birthdays.create.UiBirthdayDateFormatter
import com.elementary.tasks.birthdays.dialog.BirthdayActionViewModel
import com.elementary.tasks.birthdays.dialog.CreateBirthdayActionScreenStateUseCase
import com.elementary.tasks.birthdays.list.BirthdaysViewModel
import com.elementary.tasks.birthdays.preview.PreviewBirthdayViewModel
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val birthdaysModule = module {
  factory { UiBirthdayDateFormatter(get()) }
  factory { GetBirthdayActionsUseCase() }
  factory { CreateBirthdayActionScreenStateUseCase(get(), get(), get(), get(), get()) }

  viewModel { (id: String, isTest: Boolean) ->
    BirthdayActionViewModel(
      id,
      isTest,
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
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
  viewModel { PreviewBirthdayViewModel(get(), get(), get(), get(), get(), get()) }

  factory { DeleteBirthdayUseCase(get(), get(), get(), get(), get()) }
  factory { SaveBirthdayUseCase(get(), get(), get(), get(), get()) }
}


