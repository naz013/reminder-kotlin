package com.elementary.tasks.groups

import com.elementary.tasks.groups.create.EditGroupViewModel
import com.elementary.tasks.groups.list.GroupsViewModel
import com.elementary.tasks.groups.usecase.DeleteReminderGroupUseCase
import com.elementary.tasks.groups.usecase.SaveReminderGroupUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val reminderGroupModule = module {
  factory { DeleteReminderGroupUseCase(get(), get()) }
  factory { SaveReminderGroupUseCase(get(), get()) }

  viewModel { GroupsViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { (id: String) ->
    EditGroupViewModel(id, get(), get(), get(), get(), get(), get(), get(), get(), get())
  }
}
