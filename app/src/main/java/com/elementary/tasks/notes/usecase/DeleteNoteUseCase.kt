package com.elementary.tasks.notes.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.github.naz013.logging.Logger
import com.github.naz013.repository.NoteRepository
import com.github.naz013.sync.DataType

class DeleteNoteUseCase(
  private val noteRepository: NoteRepository,
  private val noteImageRepository: NoteImageRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {
  suspend operator fun invoke(noteId: String) {
    noteRepository.delete(noteId)
    noteRepository.deleteImageForNote(noteId)
    noteImageRepository.clearFolder(noteId)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.Notes,
      id = noteId
    )
    Logger.i(TAG, "Deleted note with id = $noteId")
  }

  companion object {
    private const val TAG = "DeleteNoteUseCase"
  }
}
