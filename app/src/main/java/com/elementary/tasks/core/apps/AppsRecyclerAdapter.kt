package com.elementary.tasks.core.apps

import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.elementary.tasks.core.file_explorer.RecyclerClickListener
import com.elementary.tasks.databinding.ListItemApplicationBinding

import java.util.ArrayList

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
class AppsRecyclerAdapter internal constructor(private val mListener: RecyclerClickListener?) : RecyclerView.Adapter<AppsRecyclerAdapter.ApplicationViewHolder>() {
    private var mData: MutableList<ApplicationItem> = ArrayList()

    var data: MutableList<ApplicationItem>
        get() = mData
        set(list) {
            this.mData = list
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun getItem(position: Int): ApplicationItem {
        return mData[position]
    }

    fun removeItem(position: Int) {
        if (position < mData.size) {
            mData.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(0, mData.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ApplicationViewHolder(ListItemApplicationBinding.inflate(inflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding!!.item = item
    }

    internal inner class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ListItemApplicationBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
            binding!!.setClick { view ->
                mListener?.onItemClick(adapterPosition)
            }
        }
    }

    companion object {

        @BindingAdapter("loadImage")
        fun loadImage(imageView: ImageView, v: Drawable) {
            imageView.setImageDrawable(v)
        }
    }
}
