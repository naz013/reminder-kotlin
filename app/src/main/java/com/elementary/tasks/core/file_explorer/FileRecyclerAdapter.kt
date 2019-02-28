package com.elementary.tasks.core.file_explorer

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.UriUtil
import com.elementary.tasks.databinding.ListItemFileBinding
import timber.log.Timber
import java.io.File

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class FileRecyclerAdapter : ListAdapter<FileItem, FileRecyclerAdapter.ContactViewHolder>(FileDiffCallback()) {

    var clickListener: ActionsListener<FileItem>? = null

    override fun submitList(list: List<FileItem>?) {
        super.submitList(list)
        notifyDataSetChanged()
    }

    fun getFileItem(position: Int): FileItem? {
        return if (position < 0 || position >= itemCount) {
            null
        } else {
            super.getItem(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(parent: ViewGroup) : HolderBinding<ListItemFileBinding>(parent, R.layout.list_item_file) {
        fun bind(fileItem: FileItem) {
            if (fileItem.isUp) {
                binding.itemName.text = itemView.context.getString(R.string.up)
            } else {
                binding.itemName.text = fileItem.fileName
            }
            loadImage(fileItem)
        }

        init {
            binding.clickView.setOnClickListener {
                clickListener?.onAction(it, adapterPosition, getFileItem(adapterPosition), ListActions.OPEN)
            }
            binding.clickView.setOnLongClickListener {
                clickListener?.onAction(it, adapterPosition, getFileItem(adapterPosition), ListActions.MORE)
                return@setOnLongClickListener true
            }
        }

        private fun loadImage(item: FileItem) {
            binding.itemImage.visibility = View.VISIBLE
            binding.itemPhoto.visibility = View.GONE
            if (item.filePath != "") {
                binding.itemImage.setImageResource(getFileIcon(File(item.filePath)))
            } else {
                binding.itemImage.setImageResource(item.icon)
            }
            if (item.filePath != "" && isPicture(item.filePath)) {
                binding.itemImage.visibility = View.GONE
                binding.itemPhoto.visibility = View.VISIBLE
                Glide.with(binding.itemPhoto.context)
                        .load(UriUtil.getUri(binding.itemPhoto.context, item.filePath))
                        .apply(RequestOptions.centerCropTransform())
                        .apply(RequestOptions.overrideOf(100, 100))
                        .into(binding.itemPhoto)
            }
        }
    }

    private fun getFileIcon(file: File): Int {
        Timber.d("getFileIcon: $file")
        return when {
            file.isDirectory -> R.drawable.ic_twotone_folder_24px
            isMelody(file.name) -> R.drawable.ic_twotone_music_note_24px
            isPicture(file.name) -> R.drawable.ic_twotone_image_24px
            isMovie(file.name) -> R.drawable.ic_twotone_movie_24px
            isGif(file.name) -> R.drawable.ic_twotone_gif_24px
            isArchive(file.name) -> R.drawable.ic_twotone_archive_24px
            isAndroid(file.name) -> R.drawable.ic_twotone_android_24px
            isCode(file.name) -> R.drawable.ic_twotone_code_24px
            else -> R.drawable.ic_twotone_insert_drive_file_24px
        }
    }

    private fun isCode(file: String): Boolean {
        return file.contains(".xml") || file.contains(".html") || file.contains(".java")
                || file.contains(".py") || file.contains(".xhtml") || file.contains(".css")
                || file.contains(".json")
    }

    private fun isArchive(file: String): Boolean {
        return file.contains(".zip") || file.contains(".rar") || file.contains(".tar.gz")
    }

    private fun isMovie(file: String): Boolean {
        return file.contains(".mov") || file.contains(".3gp") || file.contains(".avi") ||
                file.contains(".mkv") || file.contains(".vob") || file.contains(".divx") ||
                file.contains(".mp4") || file.contains(".flv")
    }

    private fun isGif(file: String): Boolean {
        return file.contains(".gif")
    }

    private fun isAndroid(file: String): Boolean {
        return file.contains(".apk")
    }

    private fun isMelody(file: String): Boolean {
        return file.contains(".mp3") || file.contains(".ogg") || file.contains(".m4a") || file.contains(".flac")
    }

    companion object {
        fun isPicture(file: String): Boolean {
            return file.contains(".jpg") || file.contains(".jpeg") || file.contains(".png")
                    || file.contains(".tiff") || file.contains(".bmp")
        }
    }
}
