package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.core.data.adapter.place.UiPlaceListAdapter
import com.github.naz013.feature.reminder.UiReminderCommonAdapter
import com.github.naz013.feature.reminder.UiReminderPlaceAdapter
import com.github.naz013.ui.googletask.GoogleTaskItemStateAdapter
import com.github.naz013.ui.group.UiGroupListAdapter
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val adapterModule = module {
  factory { UiReminderPlaceAdapter() }
  factory { UiReminderCommonAdapter(get(), get(), get(), get(), get(), get(), get()) }

  factory { GoogleTaskItemStateAdapter(get()) }

  factory { UiGroupListAdapter(get()) }

  factoryOf(::UiPlaceListAdapter)
}
