package com.elementary.tasks.google_tasks.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener

class TasksRecyclerAdapter : ListAdapter<UiGoogleTaskList, GoogleTaskHolder>(
  UiGoogleTaskListDiffCallback()
) {

  var actionsListener: ActionsListener<UiGoogleTaskList>? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoogleTaskHolder {
    return GoogleTaskHolder(parent) { view, i, listActions ->
      actionsListener?.onAction(view, i, getItem(i), listActions)
    }
  }

  override fun onBindViewHolder(holder: GoogleTaskHolder, position: Int) {
    holder.bind(getItem(position))
  }
}
