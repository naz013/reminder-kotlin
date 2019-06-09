package com.elementary.tasks.groups.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.interfaces.ActionsListener

class GroupsRecyclerAdapter : ListAdapter<ReminderGroup, GroupHolder>(GroupDiffCallback()) {

    var actionsListener: ActionsListener<ReminderGroup>? = null

    override fun submitList(list: List<ReminderGroup>?) {
        super.submitList(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
        return GroupHolder(parent) { view, i, listActions ->
            if (actionsListener != null) {
                actionsListener?.onAction(view, i, getItem(i), listActions)
            }
        }
    }

    override fun onBindViewHolder(holder: GroupHolder, position: Int) {
        holder.setData(getItem(position))
    }
}
