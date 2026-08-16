package com.github.naz013.feature.reminder.build.reminder.decompose

import com.github.naz013.feature.reminder.note.UiNoteListAdapter
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.NoteBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiFactory
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.NoteRepository

class NoteDecomposer(
  private val biFactory: BiFactory,
  private val noteRepository: NoteRepository,
  private val uiNoteListAdapter: UiNoteListAdapter,
) {
  suspend operator fun invoke(reminder: ReminderV2): List<BuilderItem<*>> {
    val note =
      reminder.noteId
        .takeIf { it.isNotEmpty() }
        ?.let { noteRepository.getById(it) }
        ?.let { uiNoteListAdapter.convert(it) }
        ?.let { biFactory.createWithValue(BiType.NOTE, it, NoteBuilderItem::class.java) }
    return listOfNotNull(note)
  }
}
