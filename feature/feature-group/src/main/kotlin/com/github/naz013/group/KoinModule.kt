package com.github.naz013.group

import com.github.naz013.group.create.EditGroupViewModel
import com.github.naz013.group.list.GroupsViewModel
import com.github.naz013.logic.group.DeleteGroupUseCase
import com.github.naz013.logic.group.MakeGroupDefaultUseCase
import com.github.naz013.logic.group.SaveGroupUseCase
import com.github.naz013.ui.notification.settings.NotificationOverrideSubtitleFormatter
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
  }
