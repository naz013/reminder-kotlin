package com.elementary.tasks.reminder.lists

import android.content.Context
import androidx.databinding.DataBindingUtil
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ListItemTaskItemCardBinding
import com.elementary.tasks.core.data.models.ShopItem

import java.util.ArrayList
import java.util.Collections

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
class ShopListRecyclerAdapter(private val mContext: Context, list: List<ShopItem>, private val listener: ActionListener?) : RecyclerView.Adapter<ShopListRecyclerAdapter.ViewHolder>() {
    private var mDataList: MutableList<ShopItem> = ArrayList()
    private var onBind: Boolean = false

    var data: List<ShopItem>
        get() = mDataList
        set(list) {
            this.mDataList = ArrayList(list)
            notifyDataSetChanged()
        }

    init {
        this.mDataList.addAll(list)
        Collections.sort(mDataList) { item, t1 -> t1.createTime!!.compareTo(item.createTime!!) }
        sort(mDataList)
    }

    fun delete(position: Int) {
        mDataList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, mDataList.size)
    }

    fun addItem(item: ShopItem) {
        mDataList.add(0, item)
        notifyItemInserted(0)
        notifyItemRangeChanged(0, mDataList.size)
    }

    fun updateData() {
        Collections.sort(mDataList) { item, t1 -> t1.createTime!!.compareTo(item.createTime!!) }
        sort(mDataList)
        notifyDataSetChanged()
    }

    private fun sort(list: MutableList<ShopItem>) {
        val pos = -1
        for (i in list.indices) {
            val item = list[i]
            if (!item.isChecked && i > pos + 1) {
                list.removeAt(i)
                list.add(pos + 1, item)
            }
        }
    }

    fun getItem(position: Int): ShopItem {
        return mDataList[position]
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var binding: ListItemTaskItemCardBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
            binding!!.clearButton.setOnClickListener { v ->
                listener?.onItemDelete(adapterPosition)
            }
            binding!!.itemCheck.setOnCheckedChangeListener { buttonView, isChecked1 ->
                if (!onBind && listener != null) {
                    listener.onItemCheck(adapterPosition, isChecked1)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemTaskItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBind = true
        val item = mDataList[position]
        val title = item.summary
        if (item.isChecked) {
            holder.binding!!.shopText.paintFlags = holder.binding!!.shopText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.binding!!.shopText.paintFlags = holder.binding!!.shopText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        holder.binding!!.itemCheck.isChecked = item.isChecked
        holder.binding!!.shopText.text = title
        if (listener == null) {
            holder.binding!!.clearButton.visibility = View.GONE
            holder.binding!!.itemCheck.isEnabled = false
            holder.binding!!.shopText.setTextColor(ViewUtils.getColor(mContext, R.color.blackPrimary))
        } else {
            holder.binding!!.itemCheck.visibility = View.VISIBLE
            holder.binding!!.clearButton.visibility = View.VISIBLE
        }
        onBind = false
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    interface ActionListener {

        fun onItemCheck(position: Int, isChecked: Boolean)

        fun onItemDelete(position: Int)
    }
}
