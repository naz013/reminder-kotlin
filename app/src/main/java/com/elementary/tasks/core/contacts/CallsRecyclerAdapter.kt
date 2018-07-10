package com.elementary.tasks.core.contacts

import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.R
import com.elementary.tasks.core.file_explorer.FilterCallback
import com.elementary.tasks.core.file_explorer.RecyclerClickListener
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.databinding.ListItemCallBinding

import java.util.ArrayList
import androidx.appcompat.widget.AppCompatTextView
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
class CallsRecyclerAdapter internal constructor(private val mContext: Context, dataItemList: List<CallsItem>, private val mListener: RecyclerClickListener?, private val mCallback: FilterCallback?) : RecyclerView.Adapter<CallsRecyclerAdapter.ContactViewHolder>() {
    private val mDataList: MutableList<CallsItem>?

    init {
        this.mDataList = ArrayList(dataItemList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(mContext)
        return ContactViewHolder(ListItemCallBinding.inflate(inflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val item = mDataList!![position]
        holder.binding!!.item = item
    }

    override fun getItemCount(): Int {
        return mDataList?.size ?: 0
    }

    internal inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var binding: ListItemCallBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
            binding!!.setClick { view ->
                mListener?.onItemClick(adapterPosition)
            }
        }
    }

    fun filter(q: String, list: List<CallsItem>) {
        val res = filter(list, q)
        animateTo(res)
        mCallback?.filter(res.size)
    }

    private fun filter(mData: List<CallsItem>?, q: String): List<CallsItem> {
        var mData = mData
        var q = q
        q = q.toLowerCase()
        if (mData == null) mData = ArrayList()
        var filteredModelList: MutableList<CallsItem> = ArrayList()
        if (q.matches("".toRegex())) {
            filteredModelList = ArrayList(mData)
        } else {
            filteredModelList.addAll(getFiltered(mData, q))
        }
        return filteredModelList
    }

    private fun getFiltered(models: List<CallsItem>, query: String): List<CallsItem> {
        val list = ArrayList<CallsItem>()
        for (model in models) {
            val text = model.numberName.toLowerCase()
            if (text.contains(query)) {
                list.add(model)
            }
        }
        return list
    }

    fun removeItem(position: Int): CallsItem {
        val model = mDataList!!.removeAt(position)
        notifyItemRemoved(position)
        return model
    }

    fun addItem(position: Int, model: CallsItem) {
        mDataList!!.add(position, model)
        notifyItemInserted(position)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val model = mDataList!!.removeAt(fromPosition)
        mDataList.add(toPosition, model)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun animateTo(models: List<CallsItem>) {
        applyAndAnimateRemovals(models)
        applyAndAnimateAdditions(models)
        applyAndAnimateMovedItems(models)
    }

    private fun applyAndAnimateRemovals(newModels: List<CallsItem>) {
        for (i in mDataList!!.indices.reversed()) {
            val model = mDataList[i]
            if (!newModels.contains(model)) {
                removeItem(i)
            }
        }
    }

    private fun applyAndAnimateAdditions(newModels: List<CallsItem>) {
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

    private fun applyAndAnimateMovedItems(newModels: List<CallsItem>) {
        for (toPosition in newModels.indices.reversed()) {
            val model = newModels[toPosition]
            val fromPosition = mDataList!!.indexOf(model)
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition)
            }
        }
    }

    fun getItem(position: Int): CallsItem? {
        return if (position < mDataList!!.size)
            mDataList[position]
        else
            null
    }

    companion object {

        @BindingAdapter("loadCallDate")
        fun loadDate(textView: AppCompatTextView, date: Long) {
            val is24 = Prefs.getInstance(textView.context).is24HourFormatEnabled
            textView.text = TimeUtil.getSimpleDateTime(date, is24)
        }

        @BindingAdapter("loadIcon")
        fun loadIcon(imageView: ImageView, type: Int) {
            val isDark = ThemeUtil.getInstance(imageView.context).isDark
            if (type == CallLog.Calls.INCOMING_TYPE) {
                imageView.setImageResource(if (isDark) R.drawable.ic_call_received_white_24dp else R.drawable.ic_call_received_black_24dp)
            } else if (type == CallLog.Calls.MISSED_TYPE) {
                imageView.setImageResource(if (isDark) R.drawable.ic_call_missed_white_24dp else R.drawable.ic_call_missed_black_24dp)
            } else {
                imageView.setImageResource(if (isDark) R.drawable.ic_call_made_white_24dp else R.drawable.ic_call_made_black_24dp)
            }
        }

        @BindingAdapter("loadCallImage")
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
