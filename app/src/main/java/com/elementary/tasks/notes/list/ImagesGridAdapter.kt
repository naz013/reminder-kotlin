package com.elementary.tasks.notes.list

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.databinding.ListItemNoteImageBinding
import com.elementary.tasks.notes.create.DecodeImages

/**
 * Copyright 2016 Nazar Suhovich
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
class ImagesGridAdapter : ListAdapter<ImageFile, ImagesGridAdapter.PhotoViewHolder>(ImageDIffCallback()) {

    var isEditable: Boolean = false
    var actionsListener: ActionsListener<ImageFile>? = null
    var data: List<ImageFile> = listOf()
        private set

    fun get(position: Int): ImageFile {
        return data[position]
    }

    override fun submitList(list: List<ImageFile>?) {
        super.submitList(list)
        if (list != null) {
            data = list
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(parent)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PhotoViewHolder(parent: ViewGroup) : HolderBinding<ListItemNoteImageBinding>(parent, R.layout.list_item_note_image) {
        fun bind(noteImage: ImageFile) {
            if (noteImage.state is DecodeImages.State.Loading) {
                binding.stateReady.visibility = View.GONE
                binding.stateLoading.visibility = View.VISIBLE
            } else {
                binding.stateLoading.visibility = View.GONE
                binding.stateReady.visibility = View.VISIBLE
                loadImage(binding.photoView, noteImage)
            }
        }

        init {
            binding.photoView.setOnClickListener { view -> performClick(view, adapterPosition) }
            if (isEditable) {
                binding.removeButton.visibility = View.VISIBLE
                binding.removeButton.setOnClickListener {
                    actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.REMOVE)
                }
                if (actionsListener != null && Module.isPro) {
                    binding.editButton.visibility = View.VISIBLE
                    binding.editButton.setOnClickListener { view ->
                        actionsListener?.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.EDIT)
                    }
                } else {
                    binding.editButton.visibility = View.GONE
                }
            } else {
                binding.removeButton.visibility = View.GONE
                binding.editButton.visibility = View.GONE
            }
        }

        private fun performClick(view: View, position: Int) {
            if (actionsListener != null) {
                actionsListener?.onAction(view, position, null, ListActions.OPEN)
            }
        }

        private fun loadImage(imageView: ImageView, image: ImageFile) {
            Glide.with(imageView).load(image.image).into(imageView)
        }
    }
}
