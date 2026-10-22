package com.github.naz013.feature.note.usecase

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteVersion
import com.github.naz013.logging.Logger
import com.github.naz013.repository.NoteVersionRepository

/**
 * Stores [previous] as a local-only version of the note when it differs from [updated], skipping the
 * snapshot if the newest version is less than [MIN_INTERVAL_MILLIS] old (unless [force]d) and keeping
 * at most [MAX_VERSIONS] versions per note.
 */
internal class SaveNoteVersionUseCase(
  private val noteVersionRepository: NoteVersionRepository,
  private val currentTimeMillis: () -> Long,
) {
  suspend operator fun invoke(
    noteKey: String,
    previous: NoteDocument,
    updated: NoteDocument,
    force: Boolean = false,
  ): Boolean {
    if (previous == updated || previous.text.isEmpty()) return false

    val now = currentTimeMillis()
    val newest = noteVersionRepository.getByNoteKey(noteKey).firstOrNull()
    if (!force && newest != null && now - newest.savedAt < MIN_INTERVAL_MILLIS) {
      Logger.d(TAG, "Skip version for note $noteKey, newest is too recent")
      return false
    }

    noteVersionRepository.add(NoteVersion(noteKey = noteKey, content = previous, savedAt = now))
    noteVersionRepository.prune(noteKey, MAX_VERSIONS)
    Logger.i(TAG, "Saved version for note $noteKey")
    return true
  }

  companion object {
    private const val TAG = "SaveNoteVersionUseCase"
    const val MAX_VERSIONS = 20
    const val MIN_INTERVAL_MILLIS = 60_000L
  }
}
