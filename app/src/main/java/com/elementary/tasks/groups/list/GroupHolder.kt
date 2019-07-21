package com.elementary.tasks.groups.list

import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemGroupBinding

class GroupHolder(parent: ViewGroup, listener: ((View, Int, ListActions) -> Unit)?) :
        HolderBinding<ListItemGroupBinding>(parent, R.layout.list_item_group) {

    init {
        binding.clickView.setOnClickListener { view ->
            listener?.invoke(view, adapterPosition, ListActions.EDIT)
        }
        binding.buttonMore.setOnClickListener { view ->
            listener?.invoke(view, adapterPosition, ListActions.MORE)
        }
    }

    fun setData(item: ReminderGroup) {
        binding.textView.text = item.groupTitle
        binding.clickView.setCardBackgroundColor(ThemeUtil.themedColor(binding.clickView.context, item.groupColor))
    }
}