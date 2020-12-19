package com.elementary.tasks.notes.create

import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.utils.BitmapUtils
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

@Parcelize
object DecodeImages : Parcelable {

  fun startDecoding(context: Context, clipData: ClipData,
                    startCount: Int = 0,
                    onLoading: (List<ImageFile>) -> Unit,
                    onReady: (Int, ImageFile) -> Unit) {
    launchDefault {
      val count = clipData.itemCount
      val emptyList = createEmpty(count)
      withUIContext {
        onLoading.invoke(emptyList)
      }
      for (i in 0 until count) {
        val item = clipData.getItemAt(i)
        val image = addImageFromUri(context, item.uri, emptyList[i])
        withUIContext {
          onReady.invoke(i + startCount, image)
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

  private fun addImageFromUri(context: Context, uri: Uri?, image: ImageFile): ImageFile {
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
    var bitmapImage: Bitmap? = null
    try {
      bitmapImage = BitmapUtils.decodeUriToBitmap(context, uri)
    } catch (e: FileNotFoundException) {
    }

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
