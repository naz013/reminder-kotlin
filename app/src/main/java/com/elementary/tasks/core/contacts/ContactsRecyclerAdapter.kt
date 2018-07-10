package com.elementary.tasks.core.contacts

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.R
import com.elementary.tasks.core.file_explorer.FilterCallback
import com.elementary.tasks.core.file_explorer.RecyclerClickListener
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemContactBinding

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
class ContactsRecyclerAdapter internal constructor(private val mContext: Context, dataItemList: List<ContactItem>, private val mListener: RecyclerClickListener?, private val mCallback: FilterCallback?) : RecyclerView.Adapter<ContactsRecyclerAdapter.ContactViewHolder>() {
    private val mDataList: MutableList<ContactItem>?

    init {
        this.mDataList = ArrayList(dataItemList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(mContext)
        return ContactViewHolder(ListItemContactBinding.inflate(inflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val item = mDataList!![position]
        holder.binding!!.item = item
    }

    override fun getItemCount(): Int {
        return mDataList?.size ?: 0
    }

    internal inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var binding: ListItemContactBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
            binding!!.setClick { view ->
                mListener?.onItemClick(adapterPosition)
            }
        }
    }

    fun filter(q: String, list: List<ContactItem>) {
        val res = filter(list, q)
        animateTo(res)
        mCallback?.filter(res.size)
    }

    private fun filter(mData: List<ContactItem>?, q: String): List<ContactItem> {
        var mData = mData
        var q = q
        q = q.toLowerCase()
        var filteredModelList: MutableList<ContactItem> = ArrayList()
        if (mData == null) mData = ArrayList()
        if (q.matches("".toRegex())) {
            filteredModelList = ArrayList(mData)
        } else {
            filteredModelList.addAll(getFiltered(mData, q))
        }
        return filteredModelList
    }

    private fun getFiltered(models: List<ContactItem>, query: String): List<ContactItem> {
        val list = ArrayList<ContactItem>()
        for (model in models) {
            val text = model.name!!.toLowerCase()
            if (text.contains(query)) {
                list.add(model)
            }
        }
        return list
    }

    fun removeItem(position: Int): ContactItem {
        val model = mDataList!!.removeAt(position)
        notifyItemRemoved(position)
        return model
    }

    fun addItem(position: Int, model: ContactItem) {
        mDataList!!.add(position, model)
        notifyItemInserted(position)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val model = mDataList!!.removeAt(fromPosition)
        mDataList.add(toPosition, model)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun animateTo(models: List<ContactItem>) {
        applyAndAnimateRemovals(models)
        applyAndAnimateAdditions(models)
        applyAndAnimateMovedItems(models)
    }

    private fun applyAndAnimateRemovals(newModels: List<ContactItem>) {
        for (i in mDataList!!.indices.reversed()) {
            val model = mDataList[i]
            if (!newModels.contains(model)) {
                removeItem(i)
            }
        }
    }

    private fun applyAndAnimateAdditions(newModels: List<ContactItem>) {
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

    private fun applyAndAnimateMovedItems(newModels: List<ContactItem>) {
        for (toPosition in newModels.indices.reversed()) {
            val model = newModels[toPosition]
            val fromPosition = mDataList!!.indexOf(model)
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition)
            }
        }
    }

    fun getItem(position: Int): ContactItem? {
        return if (position < mDataList!!.size)
            mDataList[position]
        else
            null
    }

    companion object {

        @BindingAdapter("loadContactImage")
        fun loadImage(imageView: ImageView, v: String?) {
            val isDark = ThemeUtil.getInstance(imageView.context).isDark
            if (v == null) {
                imageView.setImageResource(if (isDark) R.drawable.ic_perm_identity_white_24dp else R.drawable.ic_perm_identity_black_24dp)
                return
            }
            Glide.with(imageView)
                    .load(Uri.parse(v))
                    .apply(RequestOptions.centerCropTransform())
                    .apply(RequestOptions.overrideOf(100, 100))
                    .into(imageView)
        }
    }
}
