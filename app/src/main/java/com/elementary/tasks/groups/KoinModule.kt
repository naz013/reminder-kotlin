package com.elementary.tasks.groups

import android.os.Bundle
import com.elementary.tasks.groups.create.EditGroupViewModel
import com.elementary.tasks.groups.list.GroupsViewModel
import com.elementary.tasks.groups.usecase.DeleteReminderGroupUseCase
import com.elementary.tasks.groups.usecase.MakeGroupDefaultUseCase
import com.elementary.tasks.groups.usecase.SaveReminderGroupUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val reminderGroupModule =
  module {
    factoryOf(::DeleteReminderGroupUseCase)
    factoryOf(::SaveReminderGroupUseCase)
    factoryOf(::MakeGroupDefaultUseCase)

    viewModelOf(::GroupsViewModel)
    viewModel { (id: String, arguments: Bundle?) ->
      EditGroupViewModel(
        id,
        arguments,
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
