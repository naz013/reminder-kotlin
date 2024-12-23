package com.elementary.tasks.reminder.build.adapter.viewholder

import android.view.ViewGroup
import com.elementary.tasks.databinding.ListItemReminderBuilderNotePreviewBinding
import com.elementary.tasks.notes.list.UiNoteListAdapterCommon
import com.elementary.tasks.reminder.build.UiListNoteBuilderItem
import com.github.naz013.ui.common.view.dp2px
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.inflater
import com.github.naz013.ui.common.view.visible

class BuilderNoteViewHolder(
  parent: ViewGroup,
  private val onClickListener: BuilderViewHolderItemClickListener,
  private val onRemoveListener: BuilderViewHolderItemClickListener,
  private val common: UiNoteListAdapterCommon = UiNoteListAdapterCommon()
) : BaseBuilderViewHolder<ListItemReminderBuilderNotePreviewBinding, UiListNoteBuilderItem>(
  viewCreator = {
    ListItemReminderBuilderNotePreviewBinding.inflate(
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

  override fun bindExtra(uiListBuilderItem: UiListNoteBuilderItem) {
    val noteData = uiListBuilderItem.noteData ?: return

    common.populateNoteUi(
      uiNoteList = noteData,
      imagesViewContainer = binding.imagesView,
      textView = binding.noteTv,
      secondaryImageSize = itemView.dp2px(72),
      backgroundView = binding.bgView,
      imageClickListener = null
    )
  }
}
