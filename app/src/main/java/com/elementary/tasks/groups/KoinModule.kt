package com.elementary.tasks.groups

import com.elementary.tasks.groups.create.EditGroupViewModel
import com.elementary.tasks.groups.details.GroupDetailsViewModel
import com.elementary.tasks.groups.list.GroupsViewModel
import com.elementary.tasks.groups.usecase.DeleteGroupUseCase
import com.elementary.tasks.groups.usecase.MakeGroupDefaultUseCase
import com.elementary.tasks.groups.usecase.SaveGroupUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val groupModule =
  module {
    factoryOf(::DeleteGroupUseCase)
    factoryOf(::SaveGroupUseCase)
    factoryOf(::MakeGroupDefaultUseCase)
    factoryOf(::NotificationOverrideSubtitleFormatter)

    viewModelOf(::GroupsViewModel)
    viewModel { (id: String, fromIntentData: Boolean) ->
      EditGroupViewModel(
        id,
        fromIntentData,
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
      )
    }
    viewModel { (id: String) ->
      GroupDetailsViewModel(
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
  }
