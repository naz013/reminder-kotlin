package com.github.naz013.tags.details

import com.github.naz013.ui.birthday.UiBirthdayList
import com.github.naz013.ui.googletask.GoogleTaskItemState
import com.github.naz013.ui.note.UiNoteListItem
import com.github.naz013.ui.reminder.UiReminderList

internal sealed interface TagDetailItem {
  val id: String
  val searchText: String

  data class ReminderItem(val ui: UiReminderList) : TagDetailItem {
    override val id: String = ui.id
    override val searchText: String = "${ui.mainText.text} ${ui.secondaryText?.text.orEmpty()}"
  }

  data class NoteItem(val ui: UiNoteListItem) : TagDetailItem {
    override val id: String = ui.id
    override val searchText: String = "${ui.title} ${ui.text}"
  }

  data class BirthdayItem(val ui: UiBirthdayList) : TagDetailItem {
    override val id: String = ui.uuId
    override val searchText: String = ui.name
  }

  data class GoogleTaskItem(val ui: GoogleTaskItemState) : TagDetailItem {
    override val id: String = ui.id
    override val searchText: String = "${ui.text} ${ui.notes.orEmpty()}"
  }
}
