package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.core.appwidgets.singlenote.RecyclableUiNoteWidgetAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayPreviewAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayShowAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayWidgetListAdapter
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskPreviewAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupEditAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteEditAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteImagesAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteListSelectableAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteNotificationAdapter
import com.elementary.tasks.core.data.adapter.note.UiNotePreviewAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteWidgetAdapter
import com.elementary.tasks.core.data.adapter.place.UiPlaceEditAdapter
import com.elementary.tasks.core.data.adapter.place.UiPlaceListAdapter
import com.elementary.tasks.core.data.adapter.preset.UiPresetListAdapter
import com.elementary.tasks.core.data.adapter.reminder.UiReminderWidgetListAdapter
import org.koin.dsl.module

val adapterModule = module {
  single { UiReminderPlaceAdapter() }
  single { UiReminderCommonAdapter(get(), get(), get(), get(), get(), get()) }
  single { UiReminderListAdapter(get(), get(), get(), get(), get()) }
  single { UiReminderListsAdapter(get(), get(), get()) }
  single { UiReminderWidgetListAdapter(get(), get()) }

  single { UiBirthdayListAdapter(get(), get()) }
  single { UiBirthdayShowAdapter(get(), get()) }
  single { UiBirthdayEditAdapter() }
  single { UiBirthdayPreviewAdapter(get(), get()) }
  single { UiBirthdayWidgetListAdapter(get()) }

  single { UiGoogleTaskListAdapter(get()) }
  single { UiGoogleTaskPreviewAdapter(get(), get()) }

  single { UiGroupListAdapter(get()) }
  single { UiGroupEditAdapter() }

  single { UiUsedTimeListAdapter() }

  single { UiNoteImagesAdapter() }
  single { UiNoteEditAdapter(get()) }
  single { UiNoteListAdapter(get(), get(), get(), get()) }
  single { UiNotePreviewAdapter(get(), get(), get()) }
  single { UiNoteNotificationAdapter(get(), get()) }

  single { UiNoteListSelectableAdapter(get(), get(), get()) }
  single { UiNoteWidgetAdapter(get(), get(), get()) }
  single { RecyclableUiNoteWidgetAdapter(get(), get(), get()) }

  single { UiPlaceListAdapter(get(), get(), get()) }
  single { UiPlaceEditAdapter() }

  single { UiPresetListAdapter(get()) }
}
