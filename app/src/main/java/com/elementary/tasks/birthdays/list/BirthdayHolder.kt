package com.elementary.tasks.birthdays.list

import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.arch.BaseViewHolder
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.data.ui.UiBirthdayList
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.append
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.listOfNotEmpty
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.ListItemBirthdayBinding

class BirthdayHolder(
  parent: ViewGroup,
  currentStateHolder: CurrentStateHolder,
  showMore: Boolean = true,
  private val listener: ((View, Int, ListActions) -> Unit)? = null
) : BaseViewHolder<ListItemBirthdayBinding>(
  ListItemBirthdayBinding.inflate(parent.inflater(), parent, false),
  currentStateHolder
) {

  init {
    binding.buttonMore.visibleGone(showMore)
    binding.buttonMore.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
    binding.itemCard.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
  }

  fun setData(item: UiBirthdayList) {
    binding.eventText.text = item.name
    binding.eventNumber.visibleGone(item.number.isNotEmpty())
    binding.eventNumber.text = item.number
    binding.eventDate.text = listOfNotEmpty(
      item.nextBirthdayDateFormatted,
      "\n",
      item.ageFormatted,
      "(${item.birthdayDate})",
      "\n",
      item.remainingTimeFormatted
    ).append()
  }
}
