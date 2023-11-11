package com.elementary.tasks.home.scheduleview.viewholder

import android.content.res.ColorStateList
import android.view.ViewGroup
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemScheduleReminderAndNoteBinding
import com.elementary.tasks.home.scheduleview.ReminderAndNoteScheduleModel

class ScheduleReminderAndNoteViewHolder(
  parent: ViewGroup,
  private val common: ScheduleReminderViewHolderCommon,
  private val noteCommon: ScheduleNoteViewHolderCommon,
  private val reminderClickListener: (Int) -> Unit,
  private val noteClickListener: (Int) -> Unit
) : BaseScheduleHolder<ListItemScheduleReminderAndNoteBinding>(
  ListItemScheduleReminderAndNoteBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.todoListView.gone()
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
    common.loadContact(data.reminder as UiReminderListActive, binding.reminderPhoneView)

    data.reminder.group?.also { group ->
      binding.reminderIconView.imageTintList = ColorStateList.valueOf(group.color)
    }

    noteCommon.loadBackground(binding.bgView, data.note)
    noteCommon.loadNote(binding.noteTv, data.note)
    noteCommon.loadImages(binding.imagesContainer, data.note)
  }
}
