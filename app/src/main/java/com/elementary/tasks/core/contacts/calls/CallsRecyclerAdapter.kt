package com.elementary.tasks.core.contacts.calls

import android.net.Uri
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.list_item_call.view.*
import java.util.*
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
class CallsRecyclerAdapter internal constructor() : RecyclerView.Adapter<CallsRecyclerAdapter.ContactViewHolder>() {

    private val mDataList: MutableList<CallsItem> = mutableListOf()
    var filterCallback: ((Int) -> Unit)? = null
    var clickListener: ((Int) -> Unit)? = null

    @Inject lateinit var prefs: Prefs
    @Inject lateinit var themeUtil: ThemeUtil

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_call, parent, false))
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(mDataList[position])
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(callsItem: CallsItem) {
            if (callsItem.name == "") {
                itemView.itemName.text = callsItem.number
            } else {
                itemView.itemName.text = callsItem.name
            }
            loadImage(itemView.itemImage, callsItem.uri)
            loadDate(itemView.itemDate, callsItem.date)
            loadIcon(itemView.itemType, callsItem.type)
        }

        init {
            itemView.onClick {
                clickListener?.invoke(adapterPosition)
            }
        }
    }

    fun filter(q: String, list: List<CallsItem>) {
        val res = filter(list, q)
        animateTo(res)
        filterCallback?.invoke(res.size)
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

    private fun removeItem(position: Int): CallsItem {
        val model = mDataList.removeAt(position)
        notifyItemRemoved(position)
        return model
    }

    private fun addItem(position: Int, model: CallsItem) {
        mDataList.add(position, model)
        notifyItemInserted(position)
    }

    private fun moveItem(fromPosition: Int, toPosition: Int) {
        val model = mDataList.removeAt(fromPosition)
        mDataList.add(toPosition, model)
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun animateTo(models: List<CallsItem>) {
        applyAndAnimateRemovals(models)
        applyAndAnimateAdditions(models)
        applyAndAnimateMovedItems(models)
    }

    private fun applyAndAnimateRemovals(newModels: List<CallsItem>) {
        for (i in mDataList.indices.reversed()) {
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
            if (!mDataList.contains(model)) {
                addItem(i, model)
            }
            i++
        }
    }

    private fun applyAndAnimateMovedItems(newModels: List<CallsItem>) {
        for (toPosition in newModels.indices.reversed()) {
            val model = newModels[toPosition]
            val fromPosition = mDataList.indexOf(model)
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition)
            }
        }
    }

    fun getItem(position: Int): CallsItem {
        return mDataList[position]
    }

    fun loadImage(imageView: ImageView, v: String?) {
        val isDark = themeUtil.isDark
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

    fun loadIcon(imageView: ImageView, type: Int) {
        val isDark = themeUtil.isDark
        when (type) {
            CallLog.Calls.INCOMING_TYPE -> imageView.setImageResource(if (isDark) R.drawable.ic_call_received_white_24dp else R.drawable.ic_call_received_black_24dp)
            CallLog.Calls.MISSED_TYPE -> imageView.setImageResource(if (isDark) R.drawable.ic_call_missed_white_24dp else R.drawable.ic_call_missed_black_24dp)
            else -> imageView.setImageResource(if (isDark) R.drawable.ic_call_made_white_24dp else R.drawable.ic_call_made_black_24dp)
        }
    }

    fun loadDate(textView: AppCompatTextView, date: Long) {
        val is24 = prefs.is24HourFormatEnabled
        textView.text = TimeUtil.getSimpleDateTime(date, is24)
    }

    fun setData(it: List<CallsItem>) {
        this.mDataList.clear()
        this.mDataList.addAll(it)
        notifyDataSetChanged()
    }
}
