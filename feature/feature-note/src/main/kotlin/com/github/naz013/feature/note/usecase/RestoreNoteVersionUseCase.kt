package com.github.naz013.feature.note.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.NoteVersionRepository

/** Replaces a note's content with a stored version, snapshotting the current content first. */
internal class RestoreNoteVersionUseCase(
  private val noteRepository: NoteRepository,
  private val noteVersionRepository: NoteVersionRepository,
  private val saveNoteVersionUseCase: SaveNoteVersionUseCase,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
) {
  suspend operator fun invoke(versionId: Long): Boolean {
    val version = noteVersionRepository.getById(versionId) ?: return false
    val note = noteRepository.getById(version.noteKey)?.note ?: return false

    saveNoteVersionUseCase(note.key, note.content, version.content, force = true)
    noteRepository.save(
      note.copy(
        content = version.content,
        updatedAt = DateTimeManager.gmtDateTime,
        version = note.version + 1,
        syncState = SyncState.WaitingForUpload,
      )
    )
    noteRepository.updateSyncState(note.key, SyncState.WaitingForUpload)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.Notes,
      id = note.key,
    )
    Logger.i(TAG, "Restored version $versionId of note ${note.key}")
    return true
  }

  companion object {
    private const val TAG = "RestoreNoteVersionUseCase"
  }
}
