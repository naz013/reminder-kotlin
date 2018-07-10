package com.elementary.tasks.core.async

import android.content.Context
import android.os.AsyncTask

import com.elementary.tasks.core.utils.IoHelper

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

class BackupTask(private val mContext: Context) : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg voids: Void): Void? {
        IoHelper(mContext).backup()
        return null
    }
}
