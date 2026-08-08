package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayPreviewAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteEditAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteImagesAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteNotificationAdapter
import com.elementary.tasks.core.data.adapter.note.UiNotePreviewAdapter
import com.elementary.tasks.core.data.adapter.place.UiPlaceListAdapter
import com.elementary.tasks.core.data.adapter.preset.UiPresetListAdapter
import com.elementary.tasks.notes.list.UiNoteListItemAdapter
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

  factory { UiNoteImagesAdapter() }
  factory { UiNoteEditAdapter(get()) }
  factory { UiNoteListAdapter(get(), get(), get(), get()) }
  factory { UiNoteListItemAdapter(get(), get()) }

  factoryOf(::UiNotePreviewAdapter)
  factoryOf(::UiNoteNotificationAdapter)

  factoryOf(::UiPlaceListAdapter)

  factoryOf(::UiPresetListAdapter)
}
