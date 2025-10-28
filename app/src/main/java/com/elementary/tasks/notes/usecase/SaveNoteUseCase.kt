package com.elementary.tasks.notes.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.logging.Logger
import com.github.naz013.repository.NoteRepository
import com.github.naz013.sync.DataType

class SaveNoteUseCase(
  private val noteRepository: NoteRepository,
  private val noteImageRepository: NoteImageRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {
  suspend operator fun invoke(noteWithImages: NoteWithImages) {
    val note = noteWithImages.note ?: return
    saveImages(noteWithImages.images, note.key)
    noteRepository.save(note)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.Notes,
      id = note.key
    )
    Logger.i(TAG, "Saved note: ${note.key}")
  }

  private suspend fun saveImages(list: List<ImageFile>, id: String) {
    val oldList = noteRepository.getImagesByNoteId(id)
    for (image in oldList) {
      noteRepository.deleteImage(image.id)
    }
    noteImageRepository.moveImagesToFolder(list, id)
      .map { it.copy(noteId = id) }
      .takeIf { it.isNotEmpty() }
      ?.also { noteRepository.saveAll(it) }
  }

  companion object {
    private const val TAG = "SaveNoteUseCase"
  }
}
