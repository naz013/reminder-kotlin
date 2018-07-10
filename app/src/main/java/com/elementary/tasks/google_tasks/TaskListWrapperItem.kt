package com.elementary.tasks.google_tasks

import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList

import java.util.ArrayList

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
class TaskListWrapperItem(val taskList: GoogleTaskList, mData: List<GoogleTask>,
                          var position: Int) {
    private val mData: List<GoogleTask>

    init {
        this.mData = ArrayList(mData)
    }

    fun getmData(): List<GoogleTask> {
        return mData
    }
}
