package com.elementary.tasks.home.scheduleview.viewholder

import android.content.res.ColorStateList
import android.view.ViewGroup
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.ListItemScheduleBirthdayBinding

class ScheduleBirthdayHolder(
  parent: ViewGroup,
  private val listener: (Int) -> Unit
) : BaseScheduleHolder<ListItemScheduleBirthdayBinding>(
  ListItemScheduleBirthdayBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.itemCard.setOnClickListener {
      listener(bindingAdapterPosition)
    }
  }

  fun setData(item: UiBirthdayList) {
    binding.eventTextView.text = item.name
    binding.eventNumberView.visibleGone(item.number.isNotEmpty())
    binding.eventNumberView.text = item.number
    binding.eventTimeView.text = item.nextBirthdayTimeFormatted
    binding.eventIconView.imageTintList = ColorStateList.valueOf(item.color)
  }
}
