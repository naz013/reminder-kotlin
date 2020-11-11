package com.elementary.tasks.birthdays.list

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.databinding.ListItemBirthdayBinding
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class BirthdayHolder(
  parent: ViewGroup,
  showMore: Boolean = true,
  private val listener: ((View, Int, ListActions) -> Unit)? = null
) : HolderBinding<ListItemBirthdayBinding>(parent, R.layout.list_item_birthday), KoinComponent {

  private val prefs: Prefs by inject()

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
