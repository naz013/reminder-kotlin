package com.elementary.tasks.groups.details

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * [GroupDetailsViewModel] stays in `app` (it reuses the not-yet-extracted reminder-list UI), so its
 * DI wiring can't live in `feature-group`'s Koin module alongside the rest of the groups screens.
 */
val groupDetailsModule = module {
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
