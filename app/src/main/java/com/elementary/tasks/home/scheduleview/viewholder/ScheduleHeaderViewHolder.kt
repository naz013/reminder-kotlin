package com.elementary.tasks.home.scheduleview.viewholder

import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.github.naz013.ui.common.view.inflater
import com.elementary.tasks.databinding.ListItemScheduleHeaderBinding
import com.elementary.tasks.home.scheduleview.HeaderScheduleModel

class ScheduleHeaderViewHolder(
  parent: ViewGroup,
  private val onHeaderClickListener: (Int) -> Unit
) : HolderBinding<ListItemScheduleHeaderBinding>(
  ListItemScheduleHeaderBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.headerAddButton.setOnClickListener {
      onHeaderClickListener(bindingAdapterPosition)
    }
  }

  fun setData(data: HeaderScheduleModel) {
    binding.headerTitleView.text = data.text
  }
}
