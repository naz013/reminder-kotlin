package com.elementary.tasks.notes.create.images

import android.content.Context
import android.net.Uri
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.data.ui.note.UiNoteImageState
import com.elementary.tasks.core.utils.withUIContext
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class ImageDecoder(
  private val context: Context,
  private val dispatcherProvider: DispatcherProvider,
  private val noteImageRepository: NoteImageRepository
) {

  fun startDecoding(
    scope: CoroutineScope,
    list: List<Uri>,
    startCount: Int = 0,
    onLoading: (List<UiNoteImage>) -> Unit,
    onReady: (Int, UiNoteImage) -> Unit
  ) {
    scope.launch(dispatcherProvider.default()) {
      val emptyList = createEmpty(list.size)
      withUIContext {
        onLoading.invoke(emptyList)
      }
      list.forEachIndexed { index, uri ->
        val image = addImageFromUri(uri, emptyList[index])
        withUIContext {
          onReady.invoke(index + startCount, image)
        }
      }
    }
  }

  private fun createEmpty(count: Int): MutableList<UiNoteImage> {
    val mutableList = mutableListOf<UiNoteImage>()
    for (i in 0 until count) {
      mutableList.add(
        UiNoteImage(
          id = 0,
          fileName = UUID.randomUUID().toString(),
          state = UiNoteImageState.LOADING
        )
      )
    }
    return mutableList
  }

  private fun addImageFromUri(uri: Uri?, image: UiNoteImage): UiNoteImage {
    if (uri == null) {
      return image.copy(state = UiNoteImageState.ERROR)
    }
    val type = context.contentResolver.getType(uri) ?: ""
    Logger.d("addImageFromUri: $type")
    if (!type.contains("image")) {
      return image.copy(state = UiNoteImageState.ERROR)
    }

    val filePath = runCatching {
      context.contentResolver.openInputStream(uri)?.let {
        noteImageRepository.saveTemporaryImage(image.fileName, it)
      }
    }.getOrNull()

    return if (filePath != null) {
      Logger.d("addImageFromUri: filePath=$filePath")
      image.copy(
        filePath = filePath,
        state = UiNoteImageState.READY
      )
    } else {
      image.copy(state = UiNoteImageState.ERROR)
    }
  }
}
