package com.elementary.tasks.home.scheduleview.viewholder

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.text.applyStyles
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.core.utils.ui.transparent
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.databinding.ListItemShopItemBinding
import com.elementary.tasks.databinding.ViewListItemBadgeBinding

class ScheduleReminderViewHolderCommon {

  fun addChips(
    container: LinearLayout,
    tags: List<UiTextElement>
  ) {
    if (tags.isEmpty()) {
      return
    }

    container.visible()
    container.isFocusableInTouchMode = false
    container.isFocusable = false
    container.removeAllViewsInLayout()

    for (tag in tags) {
      val binding = ViewListItemBadgeBinding.inflate(container.inflater(), container, false)

      binding.root.text = tag.text
      binding.root.applyStyles(tag.textFormat)

      container.addView(binding.root)
    }
  }

  fun loadItems(
    reminder: UiReminderListActiveShop,
    todoListView: LinearLayout,
    isDark: Boolean,
    @ColorInt textColor: Int
  ) {
    todoListView.visible()
    todoListView.isFocusableInTouchMode = false
    todoListView.isFocusable = false
    todoListView.removeAllViewsInLayout()
    var count = 0
    for (list in reminder.shopList) {
      val bind = ListItemShopItemBinding.inflate(
        LayoutInflater.from(todoListView.context),
        todoListView,
        false
      )
      val checkView = bind.checkView
      val textView = bind.shopText
      if (list.isChecked) {
        checkView.setImageResource(R.drawable.ic_fluent_checkbox_checked)
        checkView.imageTintList = ColorStateList.valueOf(textColor)
        textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
      } else {
        checkView.setImageResource(R.drawable.ic_fluent_checkbox_unchecked)
        checkView.imageTintList = ColorStateList.valueOf(textColor)
        textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
      }
      textView.setTextColor(textColor)
      checkView.imageTintList = ColorStateList.valueOf(textColor)
      count++
      if (count == 9) {
        checkView.transparent()
        textView.text = "..."
        todoListView.addView(bind.root)
        break
      } else {
        checkView.visible()
        textView.text = list.summary
        todoListView.addView(bind.root)
      }
    }
  }
}
