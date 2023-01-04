package com.elementary.tasks.notes.create.images

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.data.ui.note.UiNoteImageState
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.io.BitmapUtils
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream

class ImageDecoder(
  private val context: Context,
  private val dispatcherProvider: DispatcherProvider
) {

  fun startDecoding(scope: CoroutineScope,
                    list: List<Uri>,
                    startCount: Int = 0,
                    onLoading: (List<UiNoteImage>) -> Unit,
                    onReady: (Int, UiNoteImage) -> Unit) {
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
          data = null,
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
    Timber.d("addImageFromUri: $type")
    if (!type.contains("image")) {
      return image.copy(state = UiNoteImageState.ERROR)
    }
    val bitmapImage: Bitmap? = runCatching {
      BitmapUtils.decodeUriToBitmap(context, uri)
    }.getOrNull()

    return if (bitmapImage != null) {
      val outputStream = ByteArrayOutputStream()
      bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
      image.copy(
        data = outputStream.toByteArray(),
        state = UiNoteImageState.READY
      )
    } else {
      image.copy(state = UiNoteImageState.ERROR)
    }
  }
}
