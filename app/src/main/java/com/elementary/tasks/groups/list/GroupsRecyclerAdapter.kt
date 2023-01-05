package com.elementary.tasks.groups.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.interfaces.ActionsListener

class GroupsRecyclerAdapter : ListAdapter<UiGroupList, GroupHolder>(UiGroupListDiffCallback()) {

  var actionsListener: ActionsListener<UiGroupList>? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
    return GroupHolder(parent) { view, i, listActions ->
      actionsListener?.onAction(view, i, getItem(i), listActions)
    }
  }

  override fun onBindViewHolder(holder: GroupHolder, position: Int) {
    holder.setData(getItem(position))
  }
}
