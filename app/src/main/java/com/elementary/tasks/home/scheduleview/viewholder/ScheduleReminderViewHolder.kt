package com.elementary.tasks.home.scheduleview.viewholder

import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.text.applyStyles
import com.github.naz013.feature.common.android.gone
import com.github.naz013.feature.common.android.inflater
import com.github.naz013.feature.common.android.visible
import com.elementary.tasks.databinding.ListItemScheduleReminderBinding
import com.elementary.tasks.home.scheduleview.data.UiReminderScheduleList

class ScheduleReminderViewHolder(
  parent: ViewGroup,
  private val common: ScheduleReminderViewHolderCommon,
  private val listener: (Int) -> Unit
) : HolderBinding<ListItemScheduleReminderBinding>(
  ListItemScheduleReminderBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.itemCard.setOnClickListener {
      listener(bindingAdapterPosition)
    }
  }

  fun setData(data: UiReminderScheduleList) {
    binding.mainTextView.text = data.mainText.text
    binding.mainTextView.applyStyles(data.mainText.textFormat)

    data.secondaryText?.run {
      binding.secondaryTextView.visible()
      binding.secondaryTextView.text = this.text
      binding.secondaryTextView.applyStyles(this.textFormat)
    } ?: run {
      binding.secondaryTextView.gone()
    }

    binding.eventTimeView.text = data.timeText.text
    binding.eventTimeView.applyStyles(data.timeText.textFormat)

    common.addChips(binding.chipGroup, data.tags)
  }
}
