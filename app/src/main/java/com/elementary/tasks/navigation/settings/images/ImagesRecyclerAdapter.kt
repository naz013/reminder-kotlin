package com.elementary.tasks.navigation.settings.images

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.models.MainImage
import com.elementary.tasks.core.network.RetrofitBuilder
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import kotlinx.android.synthetic.main.list_item_photo.view.*
import javax.inject.Inject

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
class ImagesRecyclerAdapter : RecyclerView.Adapter<ImagesRecyclerAdapter.PhotoViewHolder>() {
    private val mDataList: MutableList<MainImage> = mutableListOf()
    private var prevSelected = -1
    @Inject
    lateinit var mPrefs: Prefs
    @Inject
    lateinit var themeUtil: ThemeUtil
    var mListener: SelectListener? = null

    init {
        ReminderApp.appComponent.inject(this)
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
        return PhotoViewHolder(inflater.inflate(R.layout.list_item_photo, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = mDataList[position]
        holder.bind(item, position)

    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: MainImage, position: Int) {
            itemView.numberView.text = "#${item.id}"
            itemView.authorView.text = item.author
            loadPhoto(itemView.photoView, item.id)
            val params = itemView.card.layoutParams as GridLayoutManager.LayoutParams
            if (position < 3) {
                params.topMargin = MeasureUtils.dp2px(itemView.context, 56)
            } else {
                params.topMargin = 0
            }
            itemView.card.layoutParams = params
            if (prevSelected == position) {
                itemView.imageView3.visibility = View.VISIBLE
            } else {
                itemView.imageView3.visibility = View.GONE
            }
        }

        init {
            itemView.container.setOnClickListener { performClick(adapterPosition) }
            itemView.container.setOnLongClickListener { view ->
                mListener?.onItemLongClicked(adapterPosition, view)
                true
            }
        }
    }

    fun setItems(list: List<MainImage>) {
        mDataList.clear()
        mDataList.addAll(list)
        notifyDataSetChanged()
    }

    fun addItems(list: List<MainImage>) {
        mDataList.addAll(list)
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
                    mListener?.deselectOverItem(prevSelected)
                } else {
                    notifyItemChanged(prevSelected)
                }
            }
            prevSelected = position
            val item = mDataList[position]
            mPrefs.imageId = position
            mPrefs.imagePath = RetrofitBuilder.getImageLink(item.id)
            notifyItemChanged(position)
            mListener?.onImageSelected(true)
        }
    }

    private fun loadPhoto(imageView: ImageView, id: Long) {
        val isDark = themeUtil.isDark
        val url = RetrofitBuilder.getImageLink(id, 800, 480)
        Glide.with(imageView).load(url).into(imageView)
    }
}
