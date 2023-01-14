package com.elementary.tasks.reminder.lists.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.data.ui.UiReminderListRemoved
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

class ArchivedReminderViewHolder(
  parent: ViewGroup,
  showMore: Boolean = true,
  private val listener: ((View, Int, ListActions) -> Unit)? = null
) : BaseUiReminderListViewHolder<ListItemReminderBinding, UiReminderListRemoved>(
  ListItemReminderBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.buttonMore.visibleGone(showMore)
    binding.todoList.gone()
    binding.itemCheck.gone()
    binding.itemCard.setOnClickListener {
      listener?.invoke(
        it,
        bindingAdapterPosition,
        ListActions.OPEN
      )
    }
    binding.itemCheck.setOnClickListener {
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

  override fun setData(data: UiReminderListRemoved) {
    binding.taskText.text = data.summary
    loadDate(data)
    loadContact(data)
    loadRepeatLeft(data)
    loadGroup(data)
  }

  @SuppressLint("SetTextI18n")
  private fun loadGroup(reminder: UiReminderListRemoved) {
    val priority = reminder.priority
    val typeLabel = reminder.illustration.title
    val groupName = reminder.group?.title ?: ""
    binding.reminderTypeGroup.text = "$typeLabel ($groupName, $priority)"
  }

  private fun loadRepeatLeft(reminder: UiReminderListRemoved) {
    binding.badgesView.visible()
    binding.repeatBadge.visible()
    binding.timeToBadge.gone()

    binding.repeatBadge.text = reminder.due.repeat
    binding.timeToBadge.text = reminder.due.remaining
  }

  private fun loadDate(reminder: UiReminderListRemoved) {
    binding.taskDate.text = reminder.due.dateTime
  }

  @SuppressLint("SetTextI18n")
  private fun loadContact(reminder: UiReminderListRemoved) {
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
