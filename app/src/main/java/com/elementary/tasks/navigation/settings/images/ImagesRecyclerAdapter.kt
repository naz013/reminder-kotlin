package com.elementary.tasks.navigation.settings.images

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.elementary.tasks.core.data.models.MainImage
import com.elementary.tasks.core.network.RetrofitBuilder
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemPhotoBinding

import java.util.ArrayList
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
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

class ImagesRecyclerAdapter internal constructor(private val mContext: Context, dataItemList: List<MainImage>, private val mListener: SelectListener?) : RecyclerView.Adapter<ImagesRecyclerAdapter.PhotoViewHolder>() {
    private val mDataList: MutableList<MainImage>?
    private var prevSelected = -1
    private val mPrefs: Prefs

    init {
        this.mDataList = ArrayList(dataItemList)
        this.mPrefs = Prefs.getInstance(mContext)
    }

    internal fun deselectLast() {
        if (prevSelected != -1) {
            prevSelected = -1
            notifyItemChanged(prevSelected)
        }
    }

    internal fun setPrevSelected(prevSelected: Int) {
        this.prevSelected = prevSelected
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PhotoViewHolder(ListItemPhotoBinding.inflate(inflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = mDataList!![position]
        holder.binding!!.item = item
        val params = holder.binding!!.card.layoutParams as GridLayoutManager.LayoutParams
        if (position < 3) {
            params.topMargin = MeasureUtils.dp2px(mContext, 56)
        } else {
            params.topMargin = 0
        }
        holder.binding!!.card.layoutParams = params
        if (prevSelected == position) {
            holder.binding!!.selected = true
        } else {
            holder.binding!!.selected = false
        }
    }

    override fun getItemCount(): Int {
        return mDataList?.size ?: 0
    }

    internal inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ListItemPhotoBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
            binding!!.container.setOnClickListener { view -> performClick(adapterPosition) }
            binding!!.container.setOnLongClickListener { view ->
                mListener?.onItemLongClicked(adapterPosition, view)
                true
            }
        }
    }

    internal fun addItems(list: List<MainImage>) {
        mDataList!!.addAll(list)
        notifyItemInserted(itemCount - list.size)
    }

    private fun performClick(position: Int) {
        if (position == prevSelected) {
            prevSelected = -1
            mPrefs.imageId = -1
            mPrefs.imagePath = ""
            notifyItemChanged(prevSelected)
            mListener?.onImageSelected(false)
        } else {
            if (prevSelected != -1) {
                if (prevSelected >= itemCount && mListener != null) {
                    mListener.deselectOverItem(prevSelected)
                } else {
                    notifyItemChanged(prevSelected)
                }
            }
            prevSelected = position
            val item = mDataList!![position]
            mPrefs.imageId = position
            mPrefs.imagePath = RetrofitBuilder.getImageLink(item.id)
            notifyItemChanged(position)
            mListener?.onImageSelected(true)
        }
    }

    companion object {

        private val TAG = "ImagesRecyclerAdapter"

        @BindingAdapter("loadPhoto")
        fun loadPhoto(imageView: ImageView, id: Long) {
            val isDark = ThemeUtil.getInstance(imageView.context).isDark
            val url = RetrofitBuilder.getImageLink(id, 800, 480)
            Glide.with(imageView).load(url).into(imageView)
        }
    }
}
