package com.elementary.tasks.reminder.build.reminder.decompose

import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.NoteBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.repository.NoteRepository

class NoteDecomposer(
  private val biFactory: BiFactory,
  private val noteRepository: NoteRepository,
  private val uiNoteListAdapter: UiNoteListAdapter
) {

  suspend operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val note = reminder.noteId.takeIf { it.isNotEmpty() }
      ?.let { noteRepository.getById(it) }
      ?.let { uiNoteListAdapter.convert(it) }
      ?.let { biFactory.createWithValue(BiType.NOTE, it, NoteBuilderItem::class.java) }
    return listOfNotNull(note)
  }
}
