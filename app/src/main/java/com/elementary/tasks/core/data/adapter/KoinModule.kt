package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayPreviewAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayShowAdapter
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskPreviewAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupEditAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteEditAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteImagesAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteNotificationAdapter
import com.elementary.tasks.core.data.adapter.note.UiNotePreviewAdapter
import com.elementary.tasks.core.data.adapter.place.UiPlaceEditAdapter
import com.elementary.tasks.core.data.adapter.place.UiPlaceListAdapter
import com.elementary.tasks.core.data.adapter.preset.UiPresetListAdapter
import org.koin.dsl.module

val adapterModule = module {
  factory { UiReminderPlaceAdapter() }
  factory { UiReminderCommonAdapter(get(), get(), get(), get(), get(), get(), get()) }
  factory { UiReminderListAdapter(get(), get(), get(), get(), get()) }
  factory { UiReminderListsAdapter(get(), get(), get()) }

  factory { UiBirthdayListAdapter(get(), get(), get()) }
  factory { UiBirthdayShowAdapter(get(), get()) }
  factory { UiBirthdayEditAdapter() }
  factory { UiBirthdayPreviewAdapter(get(), get(), get()) }

  factory { UiGoogleTaskListAdapter(get()) }
  factory { UiGoogleTaskPreviewAdapter(get(), get()) }

  factory { UiGroupListAdapter(get()) }
  factory { UiGroupEditAdapter() }

  factory { UiUsedTimeListAdapter() }

  factory { UiNoteImagesAdapter() }
  factory { UiNoteEditAdapter(get()) }
  factory { UiNoteListAdapter(get(), get(), get(), get()) }
  factory { UiNotePreviewAdapter(get(), get(), get()) }
  factory { UiNoteNotificationAdapter(get(), get()) }

  factory { UiPlaceListAdapter(get(), get(), get()) }
  factory { UiPlaceEditAdapter() }

  factory { UiPresetListAdapter(get()) }
}
