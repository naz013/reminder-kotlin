package com.elementary.tasks.google_tasks.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener

class TasksRecyclerAdapter : ListAdapter<GoogleTask, GoogleTaskHolder>(GoogleTaskDiffCallback()) {

    var actionsListener: ActionsListener<GoogleTask>? = null
    var googleTaskListMap: Map<String, GoogleTaskList> = mapOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItem(position: Int): GoogleTask? {
        return try {
            super.getItem(position)
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoogleTaskHolder {
        return GoogleTaskHolder(parent) { view, i, listActions ->
            actionsListener?.onAction(view, i, getItem(i), listActions)
        }
    }

    override fun onBindViewHolder(holder: GoogleTaskHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item, googleTaskListMap)
    }
}
