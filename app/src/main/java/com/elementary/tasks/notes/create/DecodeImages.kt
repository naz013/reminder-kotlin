package com.elementary.tasks.notes.create

import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.utils.BitmapUtils
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.*

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

    fun startDecoding(context: Context, clipData: ClipData, callback: ((List<ImageFile>) -> Unit)?) {
        launchDefault {
            val list = ArrayList<ImageFile>()
            for (i in 0 until clipData.itemCount) {
                val item = clipData.getItemAt(i)
                addImageFromUri(context, list, item.uri)
            }
            withUIContext {
                callback?.invoke(list)
            }
        }
    }

    private fun addImageFromUri(context: Context, images: MutableList<ImageFile>, uri: Uri?) {
        if (uri == null) {
            return
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
            images.add(ImageFile(outputStream.toByteArray()))
        }
    }
}
