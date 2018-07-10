package com.elementary.tasks.core.file_explorer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemFileLayoutBinding

import java.io.File
import java.util.ArrayList

import androidx.databinding.BindingAdapter
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
class FileRecyclerAdapter internal constructor(private val mContext: Context, dataItemList: List<FileDataItem>, private val mListener: RecyclerClickListener?, private val mCallback: FilterCallback?) : RecyclerView.Adapter<FileRecyclerAdapter.ContactViewHolder>() {
    private val mDataList: MutableList<FileDataItem>?

    init {
        this.mDataList = ArrayList(dataItemList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(mContext)
        return ContactViewHolder(ListItemFileLayoutBinding.inflate(inflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val item = mDataList!![position]
        holder.binding!!.item = item
    }

    override fun getItemCount(): Int {
        return mDataList?.size ?: 0
    }

    internal inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ListItemFileLayoutBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
            binding!!.setClick { view ->
                mListener?.onItemClick(adapterPosition)
            }
        }
    }

    fun filter(q: String, list: List<FileDataItem>) {
        val res = filter(list, q)
        animateTo(res)
        mCallback?.filter(res.size)
    }

    private fun filter(mData: List<FileDataItem>?, q: String): List<FileDataItem> {
        var mData = mData
        var q = q
        q = q.toLowerCase()
        if (mData == null) mData = ArrayList()
        var filteredModelList: MutableList<FileDataItem> = ArrayList()
        if (q.matches("".toRegex())) {
            filteredModelList = ArrayList(mData)
        } else {
            filteredModelList.addAll(getFiltered(mData, q))
        }
        return filteredModelList
    }

    private fun getFiltered(models: List<FileDataItem>, query: String): List<FileDataItem> {
        val list = ArrayList<FileDataItem>()
        for (model in models) {
            val text = model.fileName.toLowerCase()
            if (text.contains(query)) {
                list.add(model)
            }
        }
        return list
    }

    fun getItem(position: Int): FileDataItem {
        return mDataList!![position]
    }

    fun removeItem(position: Int): FileDataItem {
        val model = mDataList!!.removeAt(position)
        notifyItemRemoved(position)
        return model
    }

    fun addItem(position: Int, model: FileDataItem) {
        mDataList!!.add(position, model)
        notifyItemInserted(position)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val model = mDataList!!.removeAt(fromPosition)
        mDataList.add(toPosition, model)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun animateTo(models: List<FileDataItem>) {
        applyAndAnimateRemovals(models)
        applyAndAnimateAdditions(models)
        applyAndAnimateMovedItems(models)
    }

    private fun applyAndAnimateRemovals(newModels: List<FileDataItem>) {
        for (i in mDataList!!.indices.reversed()) {
            val model = mDataList[i]
            if (!newModels.contains(model)) {
                removeItem(i)
            }
        }
    }

    private fun applyAndAnimateAdditions(newModels: List<FileDataItem>) {
        var i = 0
        val count = newModels.size
        while (i < count) {
            val model = newModels[i]
            if (!mDataList!!.contains(model)) {
                addItem(i, model)
            }
            i++
        }
    }

    private fun applyAndAnimateMovedItems(newModels: List<FileDataItem>) {
        for (toPosition in newModels.indices.reversed()) {
            val model = newModels[toPosition]
            val fromPosition = mDataList!!.indexOf(model)
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition)
            }
        }
    }

    companion object {

        private val TAG = "FileRecyclerAdapter"

        @BindingAdapter("loadImage")
        fun loadImage(imageView: ImageView, item: FileDataItem) {
            val isDark = ThemeUtil.getInstance(imageView.context).isDark
            if (item.filePath != null && isPicture(item.filePath)) {
                Glide.with(imageView.context)
                        .load(File(item.filePath))
                        .apply(RequestOptions.centerCropTransform())
                        .apply(RequestOptions.overrideOf(100, 100))
                        .into(imageView)
            } else {
                imageView.setImageResource(getFileIcon(item.fileName, isDark))
            }
        }

        private fun getFileIcon(file: String, isDark: Boolean): Int {
            LogUtil.d(TAG, "getFileIcon: $file")
            if (isMelody(file)) {
                LogUtil.d(TAG, "getFileIcon: isMelody")
                return if (isDark) R.drawable.ic_music_note_white_24dp else R.drawable.ic_music_note_black_24dp
            } else if (isPicture(file)) {
                LogUtil.d(TAG, "getFileIcon: isPicture")
                return if (isDark) R.drawable.ic_image_white_24dp else R.drawable.ic_image_black_24dp
            } else if (isMovie(file)) {
                LogUtil.d(TAG, "getFileIcon: isMovie")
                return if (isDark) R.drawable.ic_movie_white_24dp else R.drawable.ic_movie_black_24dp
            } else if (isGif(file)) {
                LogUtil.d(TAG, "getFileIcon: isGif")
                return if (isDark) R.drawable.ic_gif_white_24dp else R.drawable.ic_gif_black_24dp
            } else if (isArchive(file)) {
                LogUtil.d(TAG, "getFileIcon: isArchive")
                return if (isDark) R.drawable.ic_storage_white_24dp else R.drawable.ic_storage_black_24dp
            } else if (isAndroid(file)) {
                LogUtil.d(TAG, "getFileIcon: isAndroid")
                return if (isDark) R.drawable.ic_android_white_24dp else R.drawable.ic_android_black_24dp
            } else if (!file.contains(".")) {
                LogUtil.d(TAG, "getFileIcon: folder")
                return if (isDark) R.drawable.ic_folder_white_24dp else R.drawable.ic_folder_black_24dp
            } else {
                LogUtil.d(TAG, "getFileIcon: else")
                return if (isDark) R.drawable.ic_insert_drive_file_white_24dp else R.drawable.ic_insert_drive_file_black_24dp
            }
        }

        private fun isPicture(file: String): Boolean {
            return file.contains(".jpg") || file.contains(".jpeg") || file.contains(".png")
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
    }
}
