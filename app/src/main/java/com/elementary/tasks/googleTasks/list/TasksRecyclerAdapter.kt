package com.elementary.tasks.googleTasks.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener

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
class TasksRecyclerAdapter : ListAdapter<GoogleTask, GoogleTaskHolder>(GoogleTaskDiffCallback()) {

    var actionsListener: ActionsListener<GoogleTask>? = null
    var googleTaskListMap: Map<String, GoogleTaskList> = mapOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoogleTaskHolder {
        return GoogleTaskHolder(parent) { view, i, listActions ->
            actionsListener?.onAction(view, i, getItem(i), listActions)
        }
    }

    override fun onBindViewHolder(holder: GoogleTaskHolder, position: Int) {
        holder.bind(getItem(position), googleTaskListMap)
    }
}
