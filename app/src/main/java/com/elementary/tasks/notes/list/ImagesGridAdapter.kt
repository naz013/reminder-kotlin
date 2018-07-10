package com.elementary.tasks.notes.list

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemNoteImageBinding
import com.elementary.tasks.notes.create.NoteImage

import java.util.ArrayList
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView

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
class ImagesGridAdapter : RecyclerView.Adapter<ImagesGridAdapter.PhotoViewHolder>() {

    private val mDataList = ArrayList<NoteImage>()
    private var isEditable: Boolean = false
    private var actionsListener: ActionsListener<NoteImage>? = null
        set

    val data: List<NoteImage>
        get() = mDataList

    fun setEditable(editable: Boolean) {
        isEditable = editable
    }

    fun getItem(position: Int): NoteImage {
        return mDataList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(ListItemNoteImageBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        loadImage(holder.binding!!.photoView, mDataList[position])
    }

    override fun getItemCount(): Int {
        return mDataList?.size ?: 0
    }

    internal inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ListItemNoteImageBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
            binding!!.photoView.setOnClickListener { view -> performClick(view, adapterPosition) }
            if (isEditable) {
                binding!!.removeButton.visibility = View.VISIBLE
                binding!!.removeButton.setBackgroundResource(ThemeUtil.getInstance(itemView.context).indicator)
                binding!!.removeButton.setOnClickListener { view -> removeImage(adapterPosition) }
                if (actionsListener != null && Module.isPro) {
                    binding!!.editButton.visibility = View.VISIBLE
                    binding!!.editButton.setBackgroundResource(ThemeUtil.getInstance(itemView.context).indicator)
                    binding!!.editButton.setOnClickListener { view -> actionsListener!!.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.EDIT) }
                } else {
                    binding!!.editButton.visibility = View.GONE
                }
            } else {
                binding!!.removeButton.visibility = View.GONE
                binding!!.editButton.visibility = View.GONE
            }
        }
    }

    private fun removeImage(position: Int) {
        mDataList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, mDataList.size)
    }

    fun setImages(list: List<NoteImage>) {
        mDataList.clear()
        mDataList.addAll(list)
        notifyDataSetChanged()
    }

    fun addNextImages(list: List<NoteImage>) {
        mDataList.addAll(list)
        notifyItemRangeChanged(0, mDataList.size)
    }

    fun setImage(image: NoteImage, position: Int) {
        mDataList[position] = image
        notifyItemChanged(position)
    }

    fun addImage(image: NoteImage) {
        mDataList.add(image)
        notifyDataSetChanged()
    }

    private fun performClick(view: View, position: Int) {
        if (actionsListener != null) {
            actionsListener!!.onAction(view, position, null, ListActions.OPEN)
        }
    }

    fun loadImage(imageView: ImageView, image: NoteImage) {
        Thread {
            val bmp = BitmapFactory.decodeByteArray(image.image, 0, image.image!!.size)
            imageView.post { imageView.setImageBitmap(bmp) }
        }.start()
    }
}
