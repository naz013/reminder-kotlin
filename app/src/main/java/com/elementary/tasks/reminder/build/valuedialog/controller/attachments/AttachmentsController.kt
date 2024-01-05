package com.elementary.tasks.reminder.build.valuedialog.controller.attachments

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.elementary.tasks.core.os.datapicker.MultipleUriPicker
import com.elementary.tasks.databinding.BuilderItemAttachmentFilesBinding
import com.elementary.tasks.reminder.build.AttachmentsBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class AttachmentsController(
  builderItem: AttachmentsBuilderItem,
  private val attachmentFileAdapter: UriToAttachmentFileAdapter,
  private val multipleUriPicker: MultipleUriPicker
) : AbstractBindingValueController<List<String>, BuilderItemAttachmentFilesBinding>(builderItem) {

  private val adapter = EditableAttachmentsAdapter { removeFile(it) }

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemAttachmentFilesBinding {
    return BuilderItemAttachmentFilesBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.itemsListView.layoutManager = GridLayoutManager(getContext(), 4)
    binding.itemsListView.adapter = adapter

    binding.pickFilesButton.setOnClickListener {
      multipleUriPicker.pickFiles { updateFiles(it) }
    }
  }

  override fun onDataChanged(data: List<String>?) {
    super.onDataChanged(data)
    data?.let { toUris(it) }
      ?.map { attachmentFileAdapter(it) }
      ?.also { adapter.submitList(it) }
  }

  private fun toUris(strings: List<String>) = strings.map { Uri.parse(it) }

  private fun updateFiles(newFiles: List<Uri>) {
    val oldFiles = builderItem.modifier.getValue()?.let { toUris(it) } ?: emptyList()
    val newList = oldFiles + newFiles
    updateValue(newList.map { it.toString() })
    newList.map { attachmentFileAdapter(it) }.also { adapter.submitList(it) }
  }

  private fun removeFile(position: Int) {
    val oldFiles = builderItem.modifier.getValue()?.toMutableList() ?: mutableListOf()
    oldFiles.removeAt(position)
    updateValue(oldFiles)
    adapter.currentList.toMutableList().also {
      it.removeAt(position)
      adapter.submitList(it)
    }
  }
}
