package com.elementary.tasks.navigation.settings.images

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.core.utils.MemoryUtil

import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutionException

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

class SaveAsync(private val mContext: Context) : AsyncTask<String, Void, Void>() {

    override fun doInBackground(vararg strings: String): Void? {
        val path = strings[0]
        var fileName = path
        if (path.contains("=")) {
            val index = path.indexOf('=')
            fileName = path.substring(index)
        }
        val directory = MemoryUtil.imageCacheDir ?: return null
        directory!!.mkdirs()
        val file = File(directory, "$fileName.jpg")
        try {
            val bitmap: Bitmap
            if (!path.contains("=")) {
                bitmap = Glide.with(mContext).asBitmap().load(path).apply(RequestOptions.overrideOf(1280, 76)).submit().get()
            } else {
                bitmap = Glide.with(mContext).asBitmap().load(path).submit().get()
            }
            try {
                if (file.createNewFile()) {
                    val stream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        return null
    }
}
