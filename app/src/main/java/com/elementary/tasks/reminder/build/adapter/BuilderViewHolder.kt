package com.elementary.tasks.reminder.build.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.databinding.ListItemReminderBuilderBinding
import com.elementary.tasks.reminder.build.UiListBuilderItem
import com.elementary.tasks.reminder.build.UiLitBuilderItemState

typealias BuilderViewHolderItemClickListener = (position: Int) -> Unit

class BuilderViewHolder(
  parent: ViewGroup,
  private val onClickListener: BuilderViewHolderItemClickListener,
  private val onRemoveListener: BuilderViewHolderItemClickListener,
  private val binding: ListItemReminderBuilderBinding =
    ListItemReminderBuilderBinding.inflate(
      /* inflater = */ parent.inflater(),
      /* parent = */ parent,
      /* attachToParent = */ false
    )
) : RecyclerView.ViewHolder(binding.root) {

  init {
    binding.clickView.setOnClickListener {
      onClickListener(bindingAdapterPosition)
    }
    binding.removeButton.setOnClickListener {
      onRemoveListener(bindingAdapterPosition)
    }
  }

  fun bind(uiListBuilderItem: UiListBuilderItem) {
    binding.nameTextView.text = uiListBuilderItem.builderItem.title
    binding.valueTextView.text = uiListBuilderItem.value
    binding.iconView.setImageResource(uiListBuilderItem.builderItem.iconRes)

    when (uiListBuilderItem.state) {
      is UiLitBuilderItemState.EmptyState -> {
        binding.stateBadgeView.setImageResource(R.drawable.builder_badge_state_empty)
        binding.errorTextView.gone()
      }

      is UiLitBuilderItemState.DoneState -> {
        binding.stateBadgeView.setImageResource(R.drawable.builder_badge_state_ok)
        binding.errorTextView.gone()
      }

      is UiLitBuilderItemState.ErrorState -> {
        binding.stateBadgeView.setImageResource(R.drawable.builder_badge_state_error)
        binding.errorTextView.visible()
        binding.errorTextView.text = uiListBuilderItem.errorText
      }
    }
  }
}
