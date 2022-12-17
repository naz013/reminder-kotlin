package com.elementary.tasks.notes.create

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CreateNoteViewModel(
  private val imageDecoder: ImageDecoder,
  private val dispatcherProvider: DispatcherProvider
) : ViewModel(), LifecycleObserver {

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
    viewModelScope.launch(dispatcherProvider.default()) {
      val imageFile = ImageFile(state = ImageDecoder.State.Loading)
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
        state = ImageDecoder.State.Ready
      }

      list = images.value ?: listOf()
      mutable = list.toMutableList()
      mutable[position] = imageFile
      withUIContext {
        images.postValue(mutable)
      }
    }
  }

  fun addMultiple(uris: List<Uri>) {
    val count = images.value?.size ?: 0
    imageDecoder.startDecoding(viewModelScope, uris, count, {
      val list = images.value ?: listOf()
      val mutable = list.toMutableList()
      mutable.addAll(it)
      images.postValue(mutable)
    }, { i, imageFile ->
      setImage(imageFile, i)
    })
  }

  fun setImage(imageFile: ImageFile, position: Int) {
    val list = (images.value ?: listOf()).toMutableList()
    if (position < list.size) {
      if (imageFile.state == ImageDecoder.State.Error) {
        list.removeAt(position)
      } else {
        list[position] = imageFile
      }
      images.postValue(list)
    }
  }
}
