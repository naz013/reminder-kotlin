package com.elementary.tasks.core.fileExplorer

import androidx.recyclerview.widget.DiffUtil

class FileDiffCallback : DiffUtil.ItemCallback<FileItem>() {

    override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
        return oldItem.filePath == newItem.filePath
    }
}