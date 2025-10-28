package com.elementary.tasks.notes.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.github.naz013.logging.Logger
import com.github.naz013.repository.NoteRepository
import com.github.naz013.sync.DataType

class ChangeNoteArchiveStateUseCase(
  private val noteRepository: NoteRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {
  suspend operator fun invoke(id: String, archived: Boolean) {
    val noteWithImages = noteRepository.getById(id)
    if (noteWithImages == null) {
      return
    }

    val note = noteWithImages.note
    if (note == null) {
      return
    }

    note.archived = archived
    noteRepository.save(note)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.Notes,
      id = note.key
    )
    Logger.i(TAG, "Changed archive state for note: ${note.key}, archived=$archived")
  }

  companion object {
    private const val TAG = "ChangeNoteArchiveStateUseCase"
  }
}
