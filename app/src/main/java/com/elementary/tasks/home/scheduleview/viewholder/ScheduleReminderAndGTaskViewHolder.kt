package com.elementary.tasks.home.scheduleview.viewholder

import android.view.ViewGroup
import com.elementary.tasks.core.text.applyStyles
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.databinding.ListItemScheduleReminderAndGoogleBinding
import com.elementary.tasks.home.scheduleview.ReminderAndGoogleTaskScheduleModel

class ScheduleReminderAndGTaskViewHolder(
  parent: ViewGroup,
  private val common: ScheduleReminderViewHolderCommon,
  private val googleCommon: ScheduleGoogleViewHolderCommon,
  private val reminderClickListener: (Int) -> Unit,
  private val taskClickListener: (Int) -> Unit
) : BaseScheduleHolder<ListItemScheduleReminderAndGoogleBinding>(
  ListItemScheduleReminderAndGoogleBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.reminderCard.setOnClickListener {
      reminderClickListener(bindingAdapterPosition)
    }
    binding.googleTaskCard.setOnClickListener {
      taskClickListener(bindingAdapterPosition)
    }
    binding.connectionCard.setOnClickListener { }
  }

  fun setData(data: ReminderAndGoogleTaskScheduleModel) {
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

    googleCommon.loadTitle(data.googleTask, binding.googleTaskTitleView)
    googleCommon.loadNotes(data.googleTask, binding.googleTaskNoteView)
  }
}
