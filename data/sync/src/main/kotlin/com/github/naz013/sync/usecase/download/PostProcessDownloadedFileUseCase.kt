package com.github.naz013.sync.usecase.download

import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.domain.note.OldNote
import com.github.naz013.files.model.NoteV3Json
import com.github.naz013.files.model.NoteV4Json
import com.github.naz013.logging.Logger
import com.github.naz013.sync.images.PostProcessNoteV3UseCase
import com.github.naz013.sync.images.PostProcessNoteV4UseCase
import com.github.naz013.sync.images.PostProcessOldNoteUseCase

@Suppress("DEPRECATION")
internal class PostProcessDownloadedFileUseCase(
  private val postProcessNoteV4UseCase: PostProcessNoteV4UseCase,
  private val postProcessNoteV3UseCase: PostProcessNoteV3UseCase,
  private val postProcessOldNoteUseCase: PostProcessOldNoteUseCase
) {

  suspend operator fun invoke(
    cloudFileApi: CloudFileApi,
    data: Any
  ): Any {
    return when (data) {
      is OldNote -> {
        Logger.i(TAG, "Post-processing OldNote: ${data.key}")
        postProcessOldNoteUseCase(
          oldNote = data
        )
      }
      is NoteV4Json -> {
        Logger.i(TAG, "Post-processing NoteV4Json: ${data.key}")
        postProcessNoteV4UseCase(
          cloudFileApi = cloudFileApi,
          noteV4Json = data
        )
      }
      is NoteV3Json -> {
        Logger.i(TAG, "Post-processing NoteV3Json: ${data.key}")
        postProcessNoteV3UseCase(
          cloudFileApi = cloudFileApi,
          noteV3Json = data
        )
      }
      else -> data
    }
  }

  companion object {
    private const val TAG = "PostProcessDownloadedFileUseCase"
  }
}
