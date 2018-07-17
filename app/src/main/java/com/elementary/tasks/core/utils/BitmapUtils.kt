package com.elementary.tasks.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

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
object BitmapUtils {

    private const val REQUIRED_SIZE = 350

    @Throws(FileNotFoundException::class)
    fun decodeUriToBitmap(context: Context, selectedImage: Uri): Bitmap {
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(context.contentResolver.openInputStream(selectedImage), null, o)
        var widthTmp = o.outWidth
        var heightTmp = o.outHeight
        var scale = 1
        while (true) {
            if (widthTmp / 2 < REQUIRED_SIZE || heightTmp / 2 < REQUIRED_SIZE) {
                break
            }
            widthTmp /= 2
            heightTmp /= 2
            scale *= 2
        }
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        return BitmapFactory.decodeStream(context.contentResolver.openInputStream(selectedImage), null, o2)
    }
}
