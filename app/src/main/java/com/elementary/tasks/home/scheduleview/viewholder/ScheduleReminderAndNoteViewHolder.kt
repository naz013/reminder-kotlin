package com.elementary.tasks.home.scheduleview.viewholder

import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.text.applyStyles
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.databinding.ListItemScheduleReminderAndNoteBinding
import com.elementary.tasks.home.scheduleview.ReminderAndNoteScheduleModel

class ScheduleReminderAndNoteViewHolder(
  parent: ViewGroup,
  private val common: ScheduleReminderViewHolderCommon,
  private val noteCommon: ScheduleNoteViewHolderCommon,
  private val reminderClickListener: (Int) -> Unit,
  private val noteClickListener: (Int) -> Unit
) : HolderBinding<ListItemScheduleReminderAndNoteBinding>(
  ListItemScheduleReminderAndNoteBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.reminderCard.setOnClickListener {
      reminderClickListener(bindingAdapterPosition)
    }
    binding.noteCard.setOnClickListener {
      noteClickListener(bindingAdapterPosition)
    }
    binding.connectionCard.setOnClickListener { }
  }

  fun setData(data: ReminderAndNoteScheduleModel) {
    val reminder = data.reminder

    binding.mainTextView.text = reminder.mainText.text
    binding.mainTextView.applyStyles(reminder.mainText.textFormat)

    reminder.secondaryText?.run {
      binding.secondaryTextView.visible()
      binding.secondaryTextView.text = this.text
      binding.secondaryTextView.applyStyles(this.textFormat)
    } ?: run {
      binding.secondaryTextView.gone()
    }

    binding.reminderTimeView.text = reminder.timeText.text
    binding.reminderTimeView.applyStyles(reminder.timeText.textFormat)

    common.addChips(binding.chipGroup, reminder.tags)

    noteCommon.loadBackground(binding.bgView, data.note)
    noteCommon.loadNote(binding.noteTv, data.note)
    noteCommon.loadImages(binding.imagesContainer, data.note)
  }
}
