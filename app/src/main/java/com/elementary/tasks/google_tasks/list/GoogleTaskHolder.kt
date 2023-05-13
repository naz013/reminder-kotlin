package com.elementary.tasks.google_tasks.list

import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.utils.visibleInvisible
import com.elementary.tasks.databinding.ListItemGoogleTaskBinding

class GoogleTaskHolder(
  parent: ViewGroup,
  listener: ((View, Int, ListActions) -> Unit)?
) : HolderBinding<ListItemGoogleTaskBinding>(
  ListItemGoogleTaskBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.clickView.setOnClickListener {
      listener?.invoke(it, bindingAdapterPosition, ListActions.OPEN)
    }
    binding.statusIcon.setOnClickListener {
      listener?.invoke(it, bindingAdapterPosition, ListActions.SWITCH)
    }
  }

  fun bind(googleTask: UiGoogleTaskList) {
    binding.task.text = googleTask.text
    binding.note.text = googleTask.notes
    binding.note.visibleGone(!googleTask.notes.isNullOrEmpty())

    binding.taskDate.text = googleTask.dueDate
    binding.taskDate.visibleInvisible(!googleTask.dueDate.isNullOrEmpty())

    binding.statusIcon.setImageBitmap(googleTask.statusIcon)
  }
}
