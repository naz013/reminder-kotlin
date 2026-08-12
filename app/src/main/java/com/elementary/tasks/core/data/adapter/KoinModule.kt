package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayPreviewAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.adapter.place.UiPlaceListAdapter
import com.elementary.tasks.core.data.adapter.preset.UiPresetListAdapter
import com.github.naz013.ui.googletask.GoogleTaskItemStateAdapter
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val adapterModule = module {
  factory { UiReminderPlaceAdapter() }
  factory { UiReminderCommonAdapter(get(), get(), get(), get(), get(), get(), get()) }

  factory { UiBirthdayListAdapter(get(), get(), get()) }
  factory { UiBirthdayEditAdapter() }
  factory { UiBirthdayPreviewAdapter(get(), get(), get()) }

  factory { GoogleTaskItemStateAdapter(get()) }

  factory { UiGroupListAdapter(get()) }

  factory { UiNoteListAdapter(get(), get(), get(), get()) }

  factoryOf(::UiPlaceListAdapter)

  factoryOf(::UiPresetListAdapter)
}
