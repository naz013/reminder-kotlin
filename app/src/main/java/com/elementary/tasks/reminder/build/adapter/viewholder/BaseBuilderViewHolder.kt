package com.elementary.tasks.reminder.build.adapter.viewholder

import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.UiBuilderItem
import com.elementary.tasks.reminder.build.UiLitBuilderItemState

abstract class BaseBuilderViewHolder<V : ViewBinding, D : UiBuilderItem>(
  viewCreator: () -> V,
  protected val binding: V = viewCreator()
) : RecyclerView.ViewHolder(binding.root) {

  abstract fun initClickListeners()

  init {
    initClickListeners()
  }

  fun bind(uiListBuilderItem: D) {
    setTitle(uiListBuilderItem.builderItem.title)
    setValue(uiListBuilderItem.value)
    setIcon(uiListBuilderItem.builderItem.iconRes)

    when (uiListBuilderItem.state) {
      is UiLitBuilderItemState.EmptyState -> {
        setStateBadge(R.drawable.builder_badge_state_empty)
        hideError()
      }

      is UiLitBuilderItemState.DoneState -> {
        setStateBadge(R.drawable.builder_badge_state_ok)
        hideError()
      }

      is UiLitBuilderItemState.ErrorState -> {
        setStateBadge(R.drawable.builder_badge_state_error)
        showError(uiListBuilderItem.errorText)
      }
    }

    bindExtra(uiListBuilderItem)
  }

  open fun bindExtra(uiListBuilderItem: D) {}

  abstract fun showError(errorText: String)

  abstract fun hideError()

  abstract fun setStateBadge(@DrawableRes iconRes: Int)

  abstract fun setIcon(@DrawableRes iconRes: Int)

  open fun setValue(value: String) {}

  abstract fun setTitle(title: String)
}
