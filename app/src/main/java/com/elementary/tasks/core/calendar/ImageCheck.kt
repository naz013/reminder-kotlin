package com.elementary.tasks.core.calendar

import android.os.Environment

import java.io.File

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

class ImageCheck private constructor() {
    private val photos = longArrayOf(227, 226, 11, 25, 33, 10, 16, 17, 44, 71, 95, 132)

    val isSdPresent: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    fun getImage(month: Int, id: Long): String? {
        var res: String? = null
        val sdPath = Environment.getExternalStorageDirectory()
        val sdPathDr = File(sdPath.toString() + "/JustReminder/" + "image_cache")
        if (!sdPathDr.exists()) {
            sdPathDr.mkdirs()
        }
        val image = File(sdPathDr, getImageName(month, id))
        if (image.exists()) {
            res = image.toString()
        }
        return res
    }

    fun isImage(month: Int, id: Long): Boolean {
        if (isSdPresent) {
            var res = false
            val sdPath = Environment.getExternalStorageDirectory()
            val sdPathDr = File(sdPath.toString() + "/JustReminder/" + "image_cache")
            if (!sdPathDr.exists()) {
                sdPathDr.mkdirs()
            }
            val image = File(sdPathDr, getImageName(month, id))
            if (image.exists()) {
                res = true
            }
            return res
        } else {
            return false
        }
    }

    fun getImageUrl(month: Int, id: Long): String {
        return if (id != -1) {
            BASE_URL + id
        } else {
            BASE_URL + photos[month]
        }
    }

    fun getImageName(month: Int, id: Long): String {
        return if (id != -1) {
            getFileName(id)
        } else {
            getFileName(photos[month])
        }
    }

    private fun getFileName(id: Long): String {
        return "photo_$id.jpg"
    }

    companion object {

        val BASE_URL = "https://unsplash.it/1920/1080?image="

        private var instance: ImageCheck? = null

        fun getInstance(): ImageCheck {
            if (instance == null) {
                instance = ImageCheck()
            }
            return instance
        }
    }
}
