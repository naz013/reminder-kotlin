package com.elementary.tasks.reminder.build.selectordialog.params

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.inflater
import com.github.naz013.ui.common.view.visible
import com.elementary.tasks.databinding.ListItemReminderBuilderSelectorBinding
import com.elementary.tasks.reminder.build.UiSelectorItem
import com.elementary.tasks.reminder.build.UiSelectorItemState

typealias SelectorViewHolderItemClickListener = (position: Int) -> Unit

class SelectorViewHolder(
  parent: ViewGroup,
  private val onClickListener: SelectorViewHolderItemClickListener,
  private val binding: ListItemReminderBuilderSelectorBinding =
    ListItemReminderBuilderSelectorBinding.inflate(
      /* inflater = */ parent.inflater(),
      /* parent = */ parent,
      /* attachToParent = */ false
    )
) : RecyclerView.ViewHolder(binding.root) {

  fun bind(uiSelectorItem: UiSelectorItem) {
    binding.nameTextView.text = uiSelectorItem.builderItem.title
    binding.descriptionTextView.text = uiSelectorItem.builderItem.description
    binding.iconView.setImageResource(uiSelectorItem.builderItem.iconRes)
    updateState(uiSelectorItem.state)
    when (uiSelectorItem.state) {
      is UiSelectorItemState.UiSelectorAvailable -> {
        binding.stateTextView.gone()
        binding.clickView.setOnClickListener {
          onClickListener.invoke(bindingAdapterPosition)
        }
      }

      is UiSelectorItemState.UiSelectorUnavailable -> {
        binding.clickView.setOnClickListener(null)
        binding.stateTextView.visible()
        binding.stateTextView.text = uiSelectorItem.state.message
      }
    }
  }

  private fun updateState(state: UiSelectorItemState) {
    val alpha = if (state is UiSelectorItemState.UiSelectorAvailable) {
      ENABLED_ALPHA
    } else {
      DISABLED_ALPHA
    }
    setAlpha(
      alpha,
      binding.nameTextView,
      binding.iconView,
      binding.descriptionTextView
    )
  }

  private fun setAlpha(alpha: Float, vararg views: View) {
    views.forEach { it.alpha = alpha }
  }

  companion object {
    private const val ENABLED_ALPHA = 1f
    private const val DISABLED_ALPHA = 0.75f
  }
}
