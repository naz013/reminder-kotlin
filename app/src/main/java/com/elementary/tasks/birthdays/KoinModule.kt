package com.elementary.tasks.birthdays

import com.elementary.tasks.birthdays.actions.GetBirthdayActionsUseCase
import com.elementary.tasks.birthdays.create.EditBirthdayViewModel
import com.elementary.tasks.birthdays.create.UiBirthdayDateFormatter
import com.elementary.tasks.birthdays.dialog.BirthdayActionViewModel
import com.elementary.tasks.birthdays.dialog.CreateBirthdayActionScreenStateUseCase
import com.elementary.tasks.birthdays.preview.PreviewBirthdayViewModel
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val birthdaysModule = module {
  factory { UiBirthdayDateFormatter(get()) }
  factory { GetBirthdayActionsUseCase() }
  factory { CreateBirthdayActionScreenStateUseCase(get(), get(), get(), get(), get()) }
  factoryOf(::BirthdaySmartListPredicate)

  viewModel { (id: String) ->
    BirthdayActionViewModel(
      id,
      get(),
      get(),
      get(),
      get(),
      get(),
    )
  }
  viewModel { (key: BirthdaysNavKey.Edit) ->
    EditBirthdayViewModel(
      key,
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
  viewModel { (id: String) ->
    PreviewBirthdayViewModel(
      id,
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }

  factory { DeleteBirthdayUseCase(get(), get(), get(), get(), get()) }
  factory { SaveBirthdayUseCase(get(), get(), get(), get(), get()) }
}
