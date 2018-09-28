package com.elementary.tasks.core.fileExplorer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.list_item_file.view.*
import timber.log.Timber
import java.io.File
import java.util.*

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
class FileRecyclerAdapter : RecyclerView.Adapter<FileRecyclerAdapter.ContactViewHolder>() {

    private val data: MutableList<FileDataItem> = mutableListOf()
    var filterCallback: ((Int) -> Unit)? = null
    var clickListener: ((Int) -> Unit)? = null

    fun setData(list: List<FileDataItem>) {
        val diffResult = DiffUtil.calculateDiff(FileDiffCallback(this.data, list))
        this.data.clear()
        this.data.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_file, parent, false))
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(fileDataItem: FileDataItem) {
            itemView.itemName.text = fileDataItem.fileName
            loadImage(itemView.itemImage, fileDataItem)
        }

        init {
            itemView.clickView.setOnClickListener { clickListener?.invoke(adapterPosition) }
        }
    }

    fun filter(q: String, list: List<FileDataItem>) {
        val res = filter(list, q)

        filterCallback?.invoke(res.size)
    }

    private fun filter(data: List<FileDataItem>?, q: String): List<FileDataItem> {
        var mData = data
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
        return data[position]
    }

    fun loadImage(imageView: ImageView, item: FileDataItem) {
        if (item.filePath != "") {
            imageView.setImageResource(getFileIcon(File(item.filePath)))
        } else {
            imageView.setImageResource(item.icon)
        }
        if (item.filePath != "" && isPicture(item.filePath)) {
            Glide.with(imageView.context)
                    .load(File(item.filePath))
                    .apply(RequestOptions.centerCropTransform())
                    .apply(RequestOptions.overrideOf(100, 100))
                    .into(imageView)
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

    internal class FileDiffCallback(private var oldList: List<FileDataItem>, private var newList: List<FileDataItem>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val p1 = oldList[oldItemPosition]
            val p2 = newList[newItemPosition]
            return p1.fileName == p2.fileName
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val p1 = oldList[oldItemPosition]
            val p2 = newList[newItemPosition]
            return p1 == p2
        }
    }
}
