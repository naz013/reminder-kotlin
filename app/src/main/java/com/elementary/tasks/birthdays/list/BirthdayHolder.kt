package com.elementary.tasks.birthdays.list

import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.utils.ListActions
import com.github.naz013.feature.common.append
import com.github.naz013.feature.common.listOfNotEmpty
import com.github.naz013.ui.common.view.inflater
import com.github.naz013.ui.common.view.visibleGone
import com.elementary.tasks.databinding.ListItemBirthdayBinding

class BirthdayHolder(
  parent: ViewGroup,
  showMore: Boolean = true,
  private val listener: ((View, Int, ListActions) -> Unit)? = null
) : HolderBinding<ListItemBirthdayBinding>(
  ListItemBirthdayBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.buttonMore.visibleGone(showMore)
    binding.buttonMore.setOnClickListener {
      listener?.invoke(
        it,
        bindingAdapterPosition,
        ListActions.MORE
      )
    }
    binding.itemCard.setOnClickListener {
      listener?.invoke(
        it,
        bindingAdapterPosition,
        ListActions.OPEN
      )
    }
  }

  fun setData(item: UiBirthdayList) {
    binding.eventText.text = item.name
    binding.eventNumber.visibleGone(item.number.isNotEmpty())
    binding.eventNumber.text = item.number
    binding.eventDate.text = listOfNotEmpty(
      item.nextBirthdayDateFormatted,
      "\n",
      item.birthdayDateFormatted,
      ", ${item.ageFormatted}".takeIf { item.ageFormatted.isNotEmpty() },
      "\n ${item.remainingTimeFormatted}".takeIf { item.remainingTimeFormatted != null }
    ).append()
  }
}
