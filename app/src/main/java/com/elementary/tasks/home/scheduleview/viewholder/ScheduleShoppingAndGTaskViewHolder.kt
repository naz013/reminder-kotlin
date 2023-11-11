package com.elementary.tasks.home.scheduleview.viewholder

import android.content.res.ColorStateList
import android.view.ViewGroup
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemScheduleReminderAndGoogleBinding
import com.elementary.tasks.home.scheduleview.ReminderAndGoogleTaskScheduleModel

class ScheduleShoppingAndGTaskViewHolder(
  parent: ViewGroup,
  private val isDark: Boolean,
  private val common: ScheduleReminderViewHolderCommon,
  private val googleCommon: ScheduleGoogleViewHolderCommon,
  private val reminderClickListener: (Int) -> Unit,
  private val taskClickListener: (Int) -> Unit
) : BaseScheduleHolder<ListItemScheduleReminderAndGoogleBinding>(
  ListItemScheduleReminderAndGoogleBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.reminderPhoneView.gone()
    binding.reminderCard.setOnClickListener {
      reminderClickListener(bindingAdapterPosition)
    }
    binding.googleTaskCard.setOnClickListener {
      taskClickListener(bindingAdapterPosition)
    }
    binding.connectionCard.setOnClickListener { }
  }

  fun setData(data: ReminderAndGoogleTaskScheduleModel) {
    binding.reminderTextView.text = data.reminder.summary
    binding.reminderTimeView.text = data.reminder.due?.formattedTime
    common.loadItems(
      reminder = data.reminder as UiReminderListActiveShop,
      todoListView = binding.todoListView,
      isDark = isDark,
      textColor = ThemeProvider.getThemeOnSurfaceColor(itemView.context)
    )

    data.reminder.group?.also { group ->
      binding.reminderIconView.imageTintList = ColorStateList.valueOf(group.color)
    }

    googleCommon.loadTitle(data.googleTask, binding.googleTaskTitleView)
    googleCommon.loadNotes(data.googleTask, binding.googleTaskNoteView)
    googleCommon.tintIcon(data.googleTask, binding.googleTaskIconView)
  }
}
