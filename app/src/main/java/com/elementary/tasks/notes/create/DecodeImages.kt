package com.elementary.tasks.notes.create

import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.utils.BitmapUtils
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException


/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
object DecodeImages {

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
        for (i in 0 until  count) {
            mutableList.add(ImageFile().apply { this.state = DecodeImages.State.Loading })
        }
        return mutableList
    }

    private fun addImageFromUri(context: Context, uri: Uri?, image: ImageFile): ImageFile {
        if (uri == null) {
            image.state = DecodeImages.State.Error
            return image
        }
        val type = context.contentResolver.getType(uri) ?: ""
        Timber.d("addImageFromUri: $type")
        if (!type.contains("image")) {
            image.state = DecodeImages.State.Error
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
            image.state = DecodeImages.State.Ready
        } else {
            image.state = DecodeImages.State.Error
        }
        return image
    }

    sealed class State(var id: Int = 0) {

        object Loading : State(0)
        object Ready : State(1)
        object Error : State(2)
    }
}
