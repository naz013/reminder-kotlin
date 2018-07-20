package com.elementary.tasks.notes.list

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.notes.create.NoteImage
import kotlinx.android.synthetic.main.list_item_note_image.view.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.util.*

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
    var actionsListener: ActionsListener<NoteImage>? = null

    val data: List<NoteImage>
        get() = mDataList

    fun setEditable(editable: Boolean) {
        isEditable = editable
    }

    fun getItem(position: Int): NoteImage {
        return mDataList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_note_image, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(mDataList[position])
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(noteImage: NoteImage) {
            loadImage(itemView.photoView, noteImage)
        }

        init {
            itemView.photoView.setOnClickListener { view -> performClick(view, adapterPosition) }
            if (isEditable) {
                itemView.removeButton.visibility = View.VISIBLE
                itemView.removeButton.setBackgroundResource(ThemeUtil.getInstance(itemView.context).indicator)
                itemView.removeButton.setOnClickListener { removeImage(adapterPosition) }
                if (actionsListener != null && Module.isPro) {
                    itemView.editButton.visibility = View.VISIBLE
                    itemView.editButton.setBackgroundResource(ThemeUtil.getInstance(itemView.context).indicator)
                    itemView.editButton.setOnClickListener { view ->
                        actionsListener!!.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.EDIT)
                    }
                } else {
                    itemView.editButton.visibility = View.GONE
                }
            } else {
                itemView.removeButton.visibility = View.GONE
                itemView.editButton.visibility = View.GONE
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
        val imageData = image.image ?: return
        launch(CommonPool) {
            val bmp = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            withUIContext { imageView.setImageBitmap(bmp) }
        }
    }
}
