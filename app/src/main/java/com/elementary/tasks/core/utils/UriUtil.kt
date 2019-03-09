package com.elementary.tasks.core.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.crashlytics.android.Crashlytics
import com.elementary.tasks.BuildConfig
import timber.log.Timber
import java.io.File
import java.util.*


/**
 * Copyright 2017 Nazar Suhovich
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
object UriUtil {

    const val URI_MIME = "application/x-arc-uri-list"
    const val ANY_MIME = "any"
    const val IMAGE_MIME = "image/*"

    fun obtainPath(context: Context, uri: Uri, onReady: (Boolean, String?) -> Unit) {
        launchIo {
            try {
                val id = UUID.randomUUID().toString()
                val inputStream = context.contentResolver.openInputStream(uri)
                val dir = MemoryUtil.imagesDir
                if (dir == null || inputStream == null) {
                    withUIContext { onReady.invoke(false, null) }
                } else {
                    val file = File("$dir/$id")
                    Timber.d("obtainPath: $file")
                    if (file.createNewFile()) {
                        file.copyInputStreamToFile(inputStream)
                        val filePath = file.absolutePath
                        withUIContext { onReady.invoke(true, filePath) }
                    } else {
                        withUIContext { onReady.invoke(false, null) }
                    }
                }
            } catch (e: Exception) {
                Crashlytics.logException(e)
                withUIContext { onReady.invoke(false, null) }
            }
        }
    }

    fun getUri(context: Context, filePath: String): Uri? {
        Timber.d("getUri: %s", BuildConfig.APPLICATION_ID)
        return if (Module.isNougat) {
            try {
                FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", File(filePath))
            } catch (e: java.lang.Exception) {
                Crashlytics.logException(e)
                null
            }
        } else {
            Uri.fromFile(File(filePath))
        }
    }

    fun getUri(context: Context, file: File): Uri? {
        Timber.d("getUri: %s", BuildConfig.APPLICATION_ID)
        return if (Module.isNougat) {
            try {
                FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
            } catch (e: java.lang.Exception) {
                Crashlytics.logException(e)
                null
            }
        } else {
            Uri.fromFile(file)
        }
    }
}
