package com.elementary.tasks.core.utils

import android.util.Log

import com.elementary.tasks.BuildConfig

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

object LogUtil {

    private val TAG = "Reminder->"
    private val D = BuildConfig.DEBUG

    fun i(tag: String, message: String) {
        if (D) {
            Log.i(TAG + tag, message)
        }
    }

    fun e(tag: String, message: String, e: Exception) {
        if (D) {
            Log.d(TAG + tag, message + e.localizedMessage)
        }
    }

    fun v(tag: String, message: String) {
        if (D) {
            Log.v(TAG + tag, message)
        }
    }

    fun d(tag: String, message: String) {
        if (D) {
            Log.d(TAG + tag, message)
        }
    }
}
