package com.elementary.tasks.reminder

import com.elementary.tasks.reminder.create.fragments.recur.RecurBuilderViewModel
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.create.fragments.recur.preset.PresetViewModel
import com.elementary.tasks.reminder.lists.active.ActiveGpsRemindersViewModel
import com.elementary.tasks.reminder.lists.active.ActiveRemindersViewModel
import com.elementary.tasks.reminder.lists.removed.ArchiveRemindersViewModel
import com.elementary.tasks.reminder.lists.todo.ActiveTodoRemindersViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val reminderModule = module {
  viewModel { RecurBuilderViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
  viewModel { PresetViewModel(get(), get(), get()) }

  viewModel { ActiveGpsRemindersViewModel(get(), get()) }
  viewModel { ActiveRemindersViewModel(get(), get(), get(), get(), get()) }
  viewModel { ActiveTodoRemindersViewModel(get(), get(), get(), get(), get()) }
  viewModel { ArchiveRemindersViewModel(get(), get(), get(), get(), get(), get()) }

  single { ParamToTextAdapter(get(), get()) }
}
