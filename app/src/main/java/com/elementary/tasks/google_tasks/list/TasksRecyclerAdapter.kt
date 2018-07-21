package com.elementary.tasks.google_tasks.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.interfaces.ActionsListener
import java.util.*

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
class TasksRecyclerAdapter internal constructor() : RecyclerView.Adapter<GoogleTaskHolder>() {

    private var googleTasks: List<GoogleTask> = ArrayList()
    var actionsListener: ActionsListener<GoogleTask>? = null

    fun setGoogleTasks(googleTasks: List<GoogleTask>) {
        this.googleTasks = googleTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoogleTaskHolder {
        return GoogleTaskHolder(parent) { view, i, listActions ->
            actionsListener?.onAction(view, i, googleTasks[i], listActions)
        }
    }

    override fun onBindViewHolder(holder: GoogleTaskHolder, position: Int) {
        holder.bind(googleTasks[position])
    }

    override fun getItemCount(): Int {
        return googleTasks.size
    }
}
