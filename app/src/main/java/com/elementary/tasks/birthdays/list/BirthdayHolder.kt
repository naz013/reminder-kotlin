package com.elementary.tasks.birthdays.list

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.core.arch.BaseViewHolder
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.inflater
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
    if (showMore) {
      binding.buttonMore.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
      binding.buttonMore.visibility = View.VISIBLE
    } else {
      binding.buttonMore.visibility = View.GONE
    }
    binding.itemCard.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
  }

  fun setData(item: Birthday) {
    binding.eventText.text = item.name
    if (TextUtils.isEmpty(item.number)) {
      binding.eventNumber.visibility = View.GONE
    } else {
      binding.eventNumber.visibility = View.VISIBLE
      binding.eventNumber.text = item.number
    }
    loadBirthday(binding.eventDate, item.date)
  }

  private fun loadBirthday(textView: TextView, fullDate: String) {
    val is24 = prefs.is24HourFormat
    val dateItem = TimeUtil.getFutureBirthdayDate(TimeUtil.getBirthdayTime(prefs.birthdayTime), fullDate)
    if (dateItem != null) {
      var message = SuperUtil.appendString(TimeUtil.getFullDateTime(dateItem.millis, is24, prefs.appLanguage),
        "\n", TimeUtil.getAgeFormatted(textView.context, dateItem.year, dateItem.millis, prefs.appLanguage),
        " (${TimeUtil.getReadableBirthDate(fullDate, prefs.appLanguage)})")

      if (dateItem.millis > System.currentTimeMillis()) {
        message += "\n"
        message += TimeCount.getRemaining(textView.context, dateItem.millis, prefs.appLanguage)
      }
      textView.text = message
    }
  }
}
