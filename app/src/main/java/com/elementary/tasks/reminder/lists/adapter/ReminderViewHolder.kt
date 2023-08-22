package com.elementary.tasks.reminder.lists.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.data.ui.reminder.UiAppTarget
import com.elementary.tasks.core.data.ui.reminder.UiCallTarget
import com.elementary.tasks.core.data.ui.reminder.UiEmailTarget
import com.elementary.tasks.core.data.ui.reminder.UiLinkTarget
import com.elementary.tasks.core.data.ui.reminder.UiSmsTarget
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.ListItemReminderBinding

class ReminderViewHolder(
  parent: ViewGroup,
  editable: Boolean,
  showMore: Boolean = true,
  private val listener: ((View, Int, ListActions) -> Unit)? = null
) : BaseUiReminderListViewHolder<ListItemReminderBinding, UiReminderListActive>(
  ListItemReminderBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.switchWrapper.visibleGone(editable)
    binding.buttonMore.visibleGone(showMore)
    binding.todoList.gone()
    binding.itemCard.setOnClickListener {
      listener?.invoke(
        it,
        bindingAdapterPosition,
        ListActions.OPEN
      )
    }
    binding.switchWrapper.setOnClickListener {
      listener?.invoke(
        it,
        bindingAdapterPosition,
        ListActions.SWITCH
      )
    }
    binding.buttonMore.setOnClickListener {
      listener?.invoke(
        it,
        bindingAdapterPosition,
        ListActions.MORE
      )
    }
  }

  override fun setData(data: UiReminderListActive) {
    binding.taskText.text = data.summary
    loadDate(data)
    loadCheck(data)
    loadContact(data)
    loadRepeatLeft(data)
    loadGroup(data)
  }

  @SuppressLint("SetTextI18n")
  private fun loadGroup(reminder: UiReminderListActive) {
    val priority = reminder.priority
    val typeLabel = reminder.title
    val groupName = reminder.group?.title ?: ""
    binding.reminderTypeGroup.text = "$typeLabel ($groupName, $priority)"
  }

  private fun loadRepeatLeft(reminder: UiReminderListActive) {
    binding.badgesView.visible()
    binding.repeatBadge.visible()
    binding.timeToBadge.visibleGone(reminder.isRunning)

    binding.repeatBadge.text = reminder.due.repeat
    binding.timeToBadge.text = reminder.due.remaining
  }

  private fun loadDate(reminder: UiReminderListActive) {
    binding.taskDate.text = reminder.due.dateTime
  }

  private fun loadCheck(reminder: UiReminderListActive) {
    binding.itemCheck.isChecked = reminder.status.active
  }

  @SuppressLint("SetTextI18n")
  private fun loadContact(reminder: UiReminderListActive) {
    when (val target = reminder.actionTarget) {
      is UiSmsTarget -> {
        binding.reminderPhone.visible()
        if (target.name == null) {
          binding.reminderPhone.text = target.target
        } else {
          binding.reminderPhone.text = "${target.name}(${target.target})"
        }
      }
      is UiCallTarget -> {
        binding.reminderPhone.visible()
        if (target.name == null) {
          binding.reminderPhone.text = target.target
        } else {
          binding.reminderPhone.text = "${target.name}(${target.target})"
        }
      }
      is UiAppTarget -> {
        binding.reminderPhone.visible()
        binding.reminderPhone.text = "${target.name}/${target.target}"
      }
      is UiEmailTarget -> {
        binding.reminderPhone.visible()
        if (target.name == null) {
          binding.reminderPhone.text = target.target
        } else {
          binding.reminderPhone.text = "${target.name}(${target.target})"
        }
      }
      is UiLinkTarget -> {
        binding.reminderPhone.visible()
        binding.reminderPhone.text = target.target
      }
      else -> {
        binding.reminderPhone.gone()
      }
    }
  }
}
