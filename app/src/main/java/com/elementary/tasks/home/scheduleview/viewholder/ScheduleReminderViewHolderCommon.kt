package com.elementary.tasks.home.scheduleview.viewholder

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.data.ui.reminder.UiAppTarget
import com.elementary.tasks.core.data.ui.reminder.UiCallTarget
import com.elementary.tasks.core.data.ui.reminder.UiEmailTarget
import com.elementary.tasks.core.data.ui.reminder.UiLinkTarget
import com.elementary.tasks.core.data.ui.reminder.UiSmsTarget
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.transparent
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.databinding.ListItemShopItemBinding

class ScheduleReminderViewHolderCommon {

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
        if (isDark) {
          checkView.setImageResource(R.drawable.ic_check_box_white_24dp)
        } else {
          checkView.setImageResource(R.drawable.ic_check_box_black_24dp)
        }
        textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
      } else {
        if (isDark) {
          checkView.setImageResource(R.drawable.ic_check_box_outline_blank_white_24dp)
        } else {
          checkView.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp)
        }
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

  @SuppressLint("SetTextI18n")
  fun loadContact(
    reminder: UiReminderListActive,
    textView: TextView
  ) {
    when (val target = reminder.actionTarget) {
      is UiSmsTarget -> {
        textView.visible()
        if (target.name == null) {
          textView.text = target.target
        } else {
          textView.text = "${target.name}(${target.target})"
        }
      }
      is UiCallTarget -> {
        textView.visible()
        if (target.name == null) {
          textView.text = target.target
        } else {
          textView.text = "${target.name}(${target.target})"
        }
      }
      is UiAppTarget -> {
        textView.visible()
        textView.text = "${target.name}/${target.target}"
      }
      is UiEmailTarget -> {
        textView.visible()
        if (target.name == null) {
          textView.text = target.target
        } else {
          textView.text = "${target.name}(${target.target})"
        }
      }
      is UiLinkTarget -> {
        textView.visible()
        textView.text = target.target
      }
      else -> {
        textView.gone()
      }
    }
  }
}
