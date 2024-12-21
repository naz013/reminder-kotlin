package com.elementary.tasks.groups.list

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.utils.ListActions
import com.github.naz013.feature.common.android.inflater
import com.elementary.tasks.databinding.ListItemGroupBinding

class GroupHolder(
  parent: ViewGroup,
  listener: ((View, Int, ListActions) -> Unit)?
) : HolderBinding<ListItemGroupBinding>(
  ListItemGroupBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.clickView.setOnClickListener { view ->
      listener?.invoke(view, bindingAdapterPosition, ListActions.EDIT)
    }
    binding.buttonMore.setOnClickListener { view ->
      listener?.invoke(view, bindingAdapterPosition, ListActions.MORE)
    }
  }

  fun setData(item: UiGroupList) {
    binding.textView.text = item.title
    binding.colorIndicatorView.imageTintList = ColorStateList.valueOf(item.color)
  }
}
