package com.elementary.tasks.core.data.adapter

import com.github.naz013.feature.reminder.UiReminderCommonAdapter
import com.github.naz013.feature.reminder.UiReminderPlaceAdapter
import com.github.naz013.ui.googletask.GoogleTaskItemStateAdapter
import com.github.naz013.ui.group.UiGroupListAdapter
import org.koin.dsl.module

val adapterModule = module {
  factory { UiReminderPlaceAdapter() }
  factory { UiReminderCommonAdapter(get(), get(), get(), get(), get(), get(), get()) }

  factory { GoogleTaskItemStateAdapter(get()) }

  factory { UiGroupListAdapter(get()) }
}
