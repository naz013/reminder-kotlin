package com.elementary.tasks.reminder.lists.viewholder

import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.text.applyStyles
import com.github.naz013.feature.common.android.gone
import com.github.naz013.feature.common.android.inflater
import com.github.naz013.feature.common.android.visible
import com.github.naz013.feature.common.android.visibleGone
import com.elementary.tasks.databinding.ListItemReminderNewBinding
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleReminderViewHolderCommon
import com.elementary.tasks.reminder.lists.data.UiReminderList

class ReminderViewHolder(
  parent: ViewGroup,
  editable: Boolean = true,
  showMore: Boolean = true,
  private val common: ScheduleReminderViewHolderCommon,
  private val onItemClicked: (Int) -> Unit = { },
  private val onToggleClicked: (Int) -> Unit = { },
  private val onMoreClicked: (View, Int) -> Unit = { _, _ -> }
) : HolderBinding<ListItemReminderNewBinding>(
  ListItemReminderNewBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.switchWrapper.visibleGone(editable)
    binding.buttonMore.visibleGone(showMore)

    binding.itemCard.setOnClickListener { onItemClicked(bindingAdapterPosition) }
    binding.switchWrapper.setOnClickListener { onToggleClicked(bindingAdapterPosition) }
    binding.buttonMore.setOnClickListener { onMoreClicked(it, bindingAdapterPosition) }
  }

  fun bind(data: UiReminderList) {
    binding.itemCheck.isChecked = data.state.isActive

    binding.firstLineTextView.text = data.mainText.text
    binding.firstLineTextView.applyStyles(data.mainText.textFormat)

    data.secondaryText?.run {
      binding.secondLineTextView.visible()
      binding.secondLineTextView.text = this.text
      binding.secondLineTextView.applyStyles(this.textFormat)
    } ?: run {
      binding.secondLineTextView.gone()
    }

    data.tertiaryText?.run {
      binding.thirdLineTextView.visible()
      binding.thirdLineTextView.text = this.text
      binding.thirdLineTextView.applyStyles(this.textFormat)
    } ?: run {
      binding.thirdLineTextView.gone()
    }

    common.addChips(binding.chipGroup, data.tags)
  }
}
