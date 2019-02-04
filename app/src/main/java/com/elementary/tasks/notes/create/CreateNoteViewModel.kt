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
    var isReminderAttached: MutableLiveData<Boolean> = MutableLiveData()
    var images: MutableLiveData<List<ImageFile>> = MutableLiveData()

    var isLogged = false
    var isNoteEdited = false
    var isReminderEdited = false
    var editPosition = -1

    fun removeImage(position: Int) {
        val list = (images.value ?: listOf()).toMutableList()
        if (position < list.size) {
            list.removeAt(position)
            images.postValue(list)
        }
    }

    fun addBitmap(bitmap: Bitmap) {
        launchDefault {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val imageFile = ImageFile(outputStream.toByteArray())
            val list = images.value ?: listOf()
            val mutable = list.toMutableList()
            mutable.add(imageFile)
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
            list[position] = imageFile
            images.postValue(list)
        }
    }

    private fun addImageFromUri(context: Context, uri: Uri?) {
        if (uri == null) return
        launchDefault {
            var bitmapImage: Bitmap? = null
            try {
                bitmapImage = BitmapUtils.decodeUriToBitmap(context, uri)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            if (bitmapImage != null) {
                val outputStream = ByteArrayOutputStream()
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val imageFile = ImageFile(outputStream.toByteArray())
                val list = images.value ?: listOf()
                val mutable = list.toMutableList()
                mutable.add(imageFile)
                withUIContext {
                    images.postValue(mutable)
                }
            }
        }
    }
}