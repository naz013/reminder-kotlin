package com.elementary.tasks.core.additional

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemMessageBinding

import java.util.ArrayList
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

internal class SelectableTemplatesAdapter(context: Context) : RecyclerView.Adapter<SelectableTemplatesAdapter.ViewHolder>() {

    private val mDataList = ArrayList<SmsTemplate>()
    var selectedPosition = -1
        private set
    private val themeUtil: ThemeUtil

    init {
        themeUtil = ThemeUtil.getInstance(context)
    }

    fun setData(list: List<SmsTemplate>) {
        this.mDataList.clear()
        this.mDataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDataList[position]
        holder.binding!!.item = item
        if (item.isSelected) {
            holder.binding.cardView.setCardBackgroundColor(themeUtil.getColor(themeUtil.colorAccent()))
        } else {
            holder.binding.cardView.setCardBackgroundColor(themeUtil.cardStyle)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ListItemMessageBinding?

        init {
            binding = DataBindingUtil.bind(itemView)
            binding!!.root.setOnClickListener { view -> selectItem(adapterPosition) }
        }
    }

    fun getItem(position: Int): SmsTemplate {
        return mDataList[position]
    }

    fun selectItem(position: Int) {
        if (position == selectedPosition) return
        if (selectedPosition != -1 && selectedPosition < mDataList.size) {
            mDataList[selectedPosition].isSelected = false
            notifyItemChanged(selectedPosition)
        }
        this.selectedPosition = position
        if (position < mDataList.size) {
            mDataList[position].isSelected = true
            notifyItemChanged(position)
        }
    }
}
