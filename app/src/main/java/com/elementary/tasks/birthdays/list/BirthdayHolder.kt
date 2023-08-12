package com.elementary.tasks.birthdays.list

import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.append
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.listOfNotEmpty
import com.elementary.tasks.core.utils.visibleGone
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
      item.birthdayDate,
      ", ${item.ageFormatted}".takeIf { item.ageFormatted.isNotEmpty() },
      "\n",
      item.remainingTimeFormatted
    ).append()
  }
}
