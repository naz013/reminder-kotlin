package com.elementary.tasks.voice

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
data class Reply(var viewType: Int, var `object`: Any?) {

    companion object {

        const val REPLY = 0
        const val REMINDER = 1
        const val NOTE = 2
        const val PREFS = 3
        const val GROUP = 4
        const val RESPONSE = 5
        const val SHOW_MORE = 6
        const val BIRTHDAY = 7
        const val SHOPPING = 8
        const val ASK = 9
    }
}
