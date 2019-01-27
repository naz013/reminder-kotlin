package com.elementary.tasks.core.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider

import com.elementary.tasks.BuildConfig

import java.io.File

import timber.log.Timber

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

    fun getUri(context: Context, filePath: String): Uri {
        Timber.d("getUri: %s", BuildConfig.APPLICATION_ID)
        return if (Module.isNougat) {
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", File(filePath))
        } else {
            Uri.fromFile(File(filePath))
        }
    }

    fun getUri(context: Context, file: File): Uri {
        Timber.d("getUri: %s", BuildConfig.APPLICATION_ID)
        return if (Module.isNougat) {
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        } else {
            Uri.fromFile(file)
        }
    }
}
