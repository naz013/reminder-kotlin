package com.elementary.tasks.notes.create

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.utils.BitmapUtils
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.ByteArrayOutputStream

class ImageDecoder(
  private val context: Context,
  private val dispatcherProvider: DispatcherProvider
) {

  fun startDecoding(scope: CoroutineScope,
                    list: List<Uri>,
                    startCount: Int = 0,
                    onLoading: (List<ImageFile>) -> Unit,
                    onReady: (Int, ImageFile) -> Unit) {
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

  private fun createEmpty(count: Int): MutableList<ImageFile> {
    val mutableList = mutableListOf<ImageFile>()
    for (i in 0 until count) {
      mutableList.add(ImageFile().apply { this.state = State.Loading })
    }
    return mutableList
  }

  private fun addImageFromUri(uri: Uri?, image: ImageFile): ImageFile {
    if (uri == null) {
      image.state = State.Error
      return image
    }
    val type = context.contentResolver.getType(uri) ?: ""
    Timber.d("addImageFromUri: $type")
    if (!type.contains("image")) {
      image.state = State.Error
      return image
    }
    val bitmapImage: Bitmap? = runCatching {
      BitmapUtils.decodeUriToBitmap(context, uri)
    }.getOrNull()

    if (bitmapImage != null) {
      val outputStream = ByteArrayOutputStream()
      bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
      image.image = outputStream.toByteArray()
      image.state = State.Ready
    } else {
      image.state = State.Error
    }
    return image
  }

  sealed class State(var id: Int = 0) : Parcelable {

    @Parcelize
    object Loading : State(0)

    @Parcelize
    object Ready : State(1)

    @Parcelize
    object Error : State(2)
  }
}
