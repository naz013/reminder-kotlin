package com.github.naz013.feature.note.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.note.image.NoteImageRepository
import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.TagAssignmentRepository
import java.util.Random
import java.util.UUID

internal class MergeNotesUseCase(
  private val noteRepository: NoteRepository,
  private val noteImageRepository: NoteImageRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val deleteNoteUseCase: DeleteNoteUseCase,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
) {
  suspend operator fun invoke(orderedIds: List<String>) {
    if (orderedIds.size < 2) return

    val byId = noteRepository.getByIds(orderedIds).associateBy { it.note?.key }
    val ordered = orderedIds.mapNotNull { byId[it] }.filter { it.note != null }
    if (ordered.size < 2) return

    val first = ordered.first().note!!
    val mergedKey = UUID.randomUUID().toString()
    val now = DateTimeManager.gmtDateTime

    val mergedNote = first.copy(
      key = mergedKey,
      summary = mergedBody(ordered),
      date = now,
      updatedAt = now,
      uniqueId = Random().nextInt(Integer.MAX_VALUE),
      isPinned = false,
      version = 0L,
      syncState = SyncState.WaitingForUpload,
    )

    val movedImages = mergedImages(ordered, mergedKey)

    val tagIds = ordered.flatMap {
      tagAssignmentRepository.getTagsForItem(it.note!!.key, TaggedItemType.NOTE)
    }.map { it.id }.distinct()

    noteRepository.save(mergedNote)
    if (movedImages.isNotEmpty()) {
      noteRepository.saveAll(movedImages)
    }
    tagIds.forEach { tagAssignmentRepository.attach(mergedKey, TaggedItemType.NOTE, it) }
    noteRepository.updateSyncState(mergedKey, SyncState.WaitingForUpload)
    scheduleBackgroundWorkUseCase(workType = WorkType.Upload, dataType = DataType.Notes, id = mergedKey)

    // Delete originals only after their images have been copied out and the merged note is saved.
    orderedIds.forEach { deleteNoteUseCase(it) }

    Logger.i(TAG, "Merged notes $orderedIds into $mergedKey")
  }

  private fun mergedBody(ordered: List<NoteWithImages>): String =
    buildString {
      append(ordered.first().note!!.summary)
      for (noteWithImages in ordered.drop(1)) {
        val note = noteWithImages.note!!
        append('\n')
        if (note.title.isNotBlank()) {
          append(note.title)
          append('\n')
        }
        append(note.summary)
      }
    }

  private fun mergedImages(
    ordered: List<NoteWithImages>,
    mergedKey: String,
  ): List<ImageFile> {
    val usedNames = mutableSetOf<String>()
    val renamedImages = ordered.flatMap { it.images }.map { image ->
      var name = image.fileName
      while (!usedNames.add(name)) {
        name = "${UUID.randomUUID()}_$name"
      }
      image.copy(fileName = name)
    }
    return noteImageRepository
      .copyImagesToFolder(renamedImages, mergedKey)
      .map { it.copy(noteId = mergedKey) }
  }

  companion object {
    private const val TAG = "MergeNotesUseCase"
  }
}
