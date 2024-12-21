package com.elementary.tasks.reminder.build.valuedialog.controller.attachments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.github.naz013.feature.common.android.gone
import com.github.naz013.feature.common.android.visible
import com.elementary.tasks.databinding.ListItemBuilderAttachmentFileBinding

class EditableAttachmentsAdapter(
  private val onFileRemoveListener: (Int) -> Unit
) :
  ListAdapter<AttachmentFile, EditableAttachmentsAdapter.ViewHolder>(
    AttachmentFileDiffCallback()
  ) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(parent) {
      onFileRemoveListener(it)
    }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  class AttachmentFileDiffCallback : DiffUtil.ItemCallback<AttachmentFile>() {

    override fun areItemsTheSame(oldItem: AttachmentFile, newItem: AttachmentFile): Boolean {
      return oldItem.uri == newItem.uri
    }

    override fun areContentsTheSame(oldItem: AttachmentFile, newItem: AttachmentFile): Boolean {
      return oldItem == newItem
    }
  }

  class ViewHolder(
    parent: ViewGroup,
    private val binding: ListItemBuilderAttachmentFileBinding =
      ListItemBuilderAttachmentFileBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      ),
    private val onRemoveClickListener: (Int) -> Unit
  ) : RecyclerView.ViewHolder(binding.root) {

    init {
      binding.removeButton.setOnClickListener {
        onRemoveClickListener(bindingAdapterPosition)
      }
    }

    fun bind(file: AttachmentFile) {
      binding.iconImageView.setImageResource(file.icon)
      binding.nameTextView.text = file.name
      if (file.type == AttachmentType.IMAGE) {
        binding.previewImageView.visible()
        binding.previewImageView.load(file.uri)
      } else {
        binding.previewImageView.gone()
      }
    }
  }
}
