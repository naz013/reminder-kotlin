package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayPreviewAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayShowAdapter
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupEditAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteEditAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteImagesAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteNotificationAdapter
import com.elementary.tasks.core.data.adapter.note.UiNotePreviewAdapter
import com.elementary.tasks.core.data.adapter.place.UiPlaceEditAdapter
import com.elementary.tasks.core.data.adapter.place.UiPlaceListAdapter
import org.koin.dsl.module

val adapterModule = module {
  single { UiReminderPlaceAdapter() }
  single { UiReminderCommonAdapter(get(), get(), get(), get(), get()) }
  single { UiReminderPreviewAdapter(get(), get(), get(), get()) }
  single { UiReminderListAdapter(get(), get(), get()) }
  single { UiReminderListsAdapter(get(), get(), get()) }

  single { UiBirthdayListAdapter(get()) }
  single { UiBirthdayShowAdapter(get(), get()) }
  single { UiBirthdayEditAdapter() }
  single { UiBirthdayPreviewAdapter(get(), get()) }

  single { UiGoogleTaskListAdapter(get()) }

  single { UiGroupListAdapter(get()) }
  single { UiGroupEditAdapter() }

  single { UiUsedTimeListAdapter() }

  single { UiNoteImagesAdapter() }
  single { UiNoteEditAdapter(get(), get()) }
  single { UiNoteListAdapter(get(), get(), get(), get(), get()) }
  single { UiNotePreviewAdapter(get(), get(), get(), get()) }
  single { UiNoteNotificationAdapter(get(), get()) }

  single { UiPlaceListAdapter(get(), get(), get()) }
  single { UiPlaceEditAdapter() }
}
