package com.elementary.tasks.reminder.build.adapter.viewholder

import android.view.ViewGroup
import com.github.naz013.feature.common.android.gone
import com.github.naz013.feature.common.android.inflater
import com.github.naz013.feature.common.android.visible
import com.elementary.tasks.databinding.ListItemReminderBuilderBinding
import com.elementary.tasks.reminder.build.UiListBuilderItem

class BuilderViewHolder(
  parent: ViewGroup,
  private val onClickListener: BuilderViewHolderItemClickListener,
  private val onRemoveListener: BuilderViewHolderItemClickListener
) : BaseBuilderViewHolder<ListItemReminderBuilderBinding, UiListBuilderItem>(
  viewCreator = {
    ListItemReminderBuilderBinding.inflate(
      /* inflater = */ parent.inflater(),
      /* parent = */ parent,
      /* attachToParent = */ false
    )
  }
) {

  override fun initClickListeners() {
    binding.clickView.setOnClickListener {
      onClickListener(bindingAdapterPosition)
    }
    binding.removeButton.setOnClickListener {
      onRemoveListener(bindingAdapterPosition)
    }
  }

  override fun setTitle(title: String) {
    binding.nameTextView.text = title
  }

  override fun setStateBadge(iconRes: Int) {
    binding.stateBadgeView.setImageResource(iconRes)
  }

  override fun setIcon(iconRes: Int) {
    binding.iconView.setImageResource(iconRes)
  }

  override fun hideError() {
    binding.errorTextView.gone()
  }

  override fun showError(errorText: String) {
    binding.errorTextView.visible()
    binding.errorTextView.text = errorText
  }

  override fun setValue(value: String) {
    binding.valueTextView.text = value
  }
}
