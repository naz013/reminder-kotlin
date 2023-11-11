package com.elementary.tasks.home.scheduleview.viewholder

import android.content.res.ColorStateList
import android.view.ViewGroup
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemScheduleReminderAndNoteBinding
import com.elementary.tasks.home.scheduleview.ReminderAndNoteScheduleModel

class ScheduleShoppingAndNoteViewHolder(
  parent: ViewGroup,
  private val isDark: Boolean,
  private val common: ScheduleReminderViewHolderCommon,
  private val noteCommon: ScheduleNoteViewHolderCommon,
  private val reminderClickListener: (Int) -> Unit,
  private val noteClickListener: (Int) -> Unit
) : BaseScheduleHolder<ListItemScheduleReminderAndNoteBinding>(
  ListItemScheduleReminderAndNoteBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.reminderPhoneView.gone()
    binding.reminderCard.setOnClickListener {
      reminderClickListener(bindingAdapterPosition)
    }
    binding.noteCard.setOnClickListener {
      noteClickListener(bindingAdapterPosition)
    }
    binding.connectionCard.setOnClickListener { }
  }

  fun setData(data: ReminderAndNoteScheduleModel) {
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

    noteCommon.loadBackground(binding.bgView, data.note)
    noteCommon.loadNote(binding.noteTv, data.note)
    noteCommon.loadImages(binding.imagesContainer, data.note)
  }
}
