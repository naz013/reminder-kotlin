package com.elementary.tasks.reminder

import com.elementary.tasks.reminder.create.fragments.recur.RecurBuilderViewModel
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val reminderModule = module {
  viewModel { RecurBuilderViewModel(get(), get(), get(), get()) }

  single { ParamToTextAdapter(get(), get()) }
}
