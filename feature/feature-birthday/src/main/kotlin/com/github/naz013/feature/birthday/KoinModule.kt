package com.github.naz013.feature.birthday

import com.github.naz013.feature.birthday.actions.GetBirthdayActionsUseCase
import com.github.naz013.feature.birthday.create.EditBirthdayViewModel
import com.github.naz013.feature.birthday.dialog.BirthdayActionViewModel
import com.github.naz013.feature.birthday.dialog.CreateBirthdayActionScreenStateUseCase
import com.github.naz013.feature.birthday.list.BirthdaysViewModel
import com.github.naz013.feature.birthday.preview.PreviewBirthdayViewModel
import com.github.naz013.feature.birthday.settings.BirthdaySettingsViewModel
import com.github.naz013.feature.birthday.settings.usecase.GetContactsWithMetadataUseCase
import com.github.naz013.feature.birthday.settings.work.CheckBirthdaysTask
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val featureBirthdayModule = module {
  factory { GetBirthdayActionsUseCase() }
  factory { CreateBirthdayActionScreenStateUseCase(get(), get(), get(), get(), get()) }
  factoryOf(::GetContactsWithMetadataUseCase)

  factory<BackgroundTask>(named(CheckBirthdaysTask.TASK_KEY)) {
    CheckBirthdaysTask(get(), get(), get(), get(), get())
  }

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
      get(),
      get(),
    )
  }

  viewModelOf(::BirthdaySettingsViewModel)
  viewModelOf(::BirthdaysViewModel)
}
