package com.elementary.tasks.notes.create

import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.utils.BitmapUtils
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class CreateNoteViewModel : ViewModel(), LifecycleObserver {

  var date: MutableLiveData<Long> = MutableLiveData()
  var time: MutableLiveData<Long> = MutableLiveData()
  var colorOpacity: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
  var fontStyle: MutableLiveData<Int> = MutableLiveData()
  var palette: MutableLiveData<Int> = MutableLiveData()
  var isReminderAttached: MutableLiveData<Boolean> = MutableLiveData()
  var images: MutableLiveData<List<ImageFile>> = MutableLiveData()

  var isLogged = false
  var isNoteEdited = false
  var isReminderEdited = false
  var editPosition = -1
  var isFromFile: Boolean = false

  fun removeImage(position: Int) {
    val list = (images.value ?: listOf()).toMutableList()
    if (position < list.size) {
      list.removeAt(position)
      images.postValue(list)
    }
  }

  fun addBitmap(bitmap: Bitmap) {
    launchDefault {
      val imageFile = ImageFile(state = DecodeImages.State.Loading)
      var list = images.value ?: listOf()
      var mutable = list.toMutableList()
      val position = mutable.size
      mutable.add(imageFile)
      withUIContext {
        images.postValue(mutable)
      }

      val outputStream = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
      imageFile.apply {
        image = outputStream.toByteArray()
        state = DecodeImages.State.Ready
      }

      list = images.value ?: listOf()
      mutable = list.toMutableList()
      mutable[position] = imageFile
      withUIContext {
        images.postValue(mutable)
      }
    }
  }

  fun addMultiple(uri: Uri?, clipData: ClipData?, context: Context) {
    if (uri != null) {
      addImageFromUri(context, uri)
    } else if (clipData != null) {
      val count = images.value?.size ?: 0
      DecodeImages.startDecoding(context, clipData, count, {
        val list = images.value ?: listOf()
        val mutable = list.toMutableList()
        mutable.addAll(it)
        images.postValue(mutable)
      }, { i, imageFile ->
        setImage(imageFile, i)
      })
    }
  }

  fun setImage(imageFile: ImageFile, position: Int) {
    val list = (images.value ?: listOf()).toMutableList()
    if (position < list.size) {
      if (imageFile.state == DecodeImages.State.Error) {
        list.removeAt(position)
      } else {
        list[position] = imageFile
      }
      images.postValue(list)
    }
  }

  private fun addImageFromUri(context: Context, uri: Uri?) {
    if (uri == null) return
    launchDefault {
      val imageFile = ImageFile(state = DecodeImages.State.Loading)
      var list = images.value ?: listOf()
      var mutable = list.toMutableList()
      mutable.add(imageFile)
      val position = mutable.size - 1
      if (position < 0) {
        return@launchDefault
      } else {
        withUIContext {
          images.postValue(mutable)
        }
      }
      var bitmapImage: Bitmap? = null
      try {
        bitmapImage = BitmapUtils.decodeUriToBitmap(context, uri)
      } catch (e: FileNotFoundException) {
        e.printStackTrace()
      }
      if (bitmapImage != null) {
        val outputStream = ByteArrayOutputStream()
        bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

        imageFile.apply {
          image = outputStream.toByteArray()
          state = DecodeImages.State.Ready
        }

        list = images.value ?: listOf()
        if (list.isNotEmpty()) {
          val pos = findPos(list, imageFile)
          if (pos != -1) {
            mutable = list.toMutableList()
            mutable[pos] = imageFile
            withUIContext {
              images.postValue(mutable)
            }
          }
        }
      }
    }
  }

  private fun findPos(list: List<ImageFile>, imageFile: ImageFile): Int {
    if (list.isEmpty()) return -1
    for (i in list.indices) {
      if (list[i].uuid == imageFile.uuid) {
        return i
      }
    }
    return -1
  }
}