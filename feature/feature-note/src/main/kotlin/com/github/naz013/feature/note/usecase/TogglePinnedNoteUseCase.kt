package com.github.naz013.feature.note.usecase

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.NoteRepository

internal class TogglePinnedNoteUseCase(
  private val noteRepository: NoteRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
) {
  suspend operator fun invoke(id: String) {
    val noteWithImages = noteRepository.getById(id)
    if (noteWithImages == null) {
      return
    }

    val note = noteWithImages.note
    if (note == null) {
      return
    }

    note.isPinned = !note.isPinned
    noteRepository.save(note.copy(version = note.version + 1))
    noteRepository.updateSyncState(id, SyncState.WaitingForUpload)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.Notes,
      id = note.key,
    )
    Logger.i(TAG, "Toggled pinned state for note: ${note.key}, isPinned=${note.isPinned}")
  }

  companion object {
    private const val TAG = "TogglePinnedNoteUseCase"
  }
}
