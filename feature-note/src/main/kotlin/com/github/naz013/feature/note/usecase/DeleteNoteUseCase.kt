package com.github.naz013.feature.note.usecase

import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.feature.note.image.NoteImageRepository
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.TagAssignmentRepository

class DeleteNoteUseCase(
  private val noteRepository: NoteRepository,
  private val noteImageRepository: NoteImageRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
  private val tagAssignmentRepository: TagAssignmentRepository,
) {
  suspend operator fun invoke(noteId: String) {
    noteRepository.delete(noteId)
    noteRepository.deleteImageForNote(noteId)
    noteImageRepository.clearFolder(noteId)
    tagAssignmentRepository.detachAll(noteId, TaggedItemType.NOTE)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.Notes,
      id = noteId,
    )
    Logger.i(TAG, "Deleted note with id = $noteId")
  }

  companion object {
    private const val TAG = "DeleteNoteUseCase"
  }
}
