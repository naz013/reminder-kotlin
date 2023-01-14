package com.elementary.tasks.reminder.lists.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.data.ui.UiReminderListRemovedGps
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
import java.util.Locale

class ArchivedGpsViewHolder(
  parent: ViewGroup,
  editable: Boolean,
  showMore: Boolean = true,
  private val listener: ((View, Int, ListActions) -> Unit)? = null
) : BaseUiReminderListViewHolder<ListItemReminderBinding, UiReminderListRemovedGps>(
  ListItemReminderBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.itemCheck.visibleGone(editable)
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

  override fun setData(data: UiReminderListRemovedGps) {
    binding.taskText.text = data.summary
    loadPlaces(data)
    loadContact(data)
    binding.badgesView.gone()
    loadGroup(data)
  }

  @SuppressLint("SetTextI18n")
  private fun loadGroup(reminder: UiReminderListRemovedGps) {
    val priority = reminder.priority
    val typeLabel = reminder.illustration.title
    val groupName = reminder.group?.title ?: ""
    binding.reminderTypeGroup.text = "$typeLabel ($groupName, $priority)"
  }

  private fun loadPlaces(reminder: UiReminderListRemovedGps) {
    reminder.places.firstOrNull()?.also {
      binding.taskDate.text = String.format(
        Locale.getDefault(),
        "%.5f %.5f (%d)",
        it.latitude,
        it.longitude,
        reminder.places.size
      )
    }
  }

  @SuppressLint("SetTextI18n")
  private fun loadContact(reminder: UiReminderListRemovedGps) {
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
