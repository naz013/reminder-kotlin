package com.elementary.tasks.core.additional

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.ThemeUtil
import kotlinx.android.synthetic.main.list_item_message.view.*

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
class SelectableTemplatesAdapter : RecyclerView.Adapter<SelectableTemplatesAdapter.ViewHolder>() {

    private val mDataList = mutableListOf<SmsTemplate>()
    var selectedPosition = -1
        private set

    fun setData(list: List<SmsTemplate>) {
        this.mDataList.clear()
        this.mDataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_message, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: SmsTemplate) {
            itemView.messageView.text = item.title
            if (item.isSelected) {
                itemView.bgView.setBackgroundColor(ThemeUtil.colorWithAlpha(ThemeUtil.getThemeSecondaryColor(itemView.context), 12))
            } else {
                itemView.bgView.setBackgroundResource(android.R.color.transparent)
            }
        }

        init {
            itemView.clickView.setOnClickListener { selectItem(adapterPosition) }
            itemView.buttonMore.visibility = View.GONE
        }
    }

    fun getItem(position: Int): SmsTemplate? {
        return if (position < mDataList.size) {
           try {
               mDataList[position]
           } catch (e: Exception) {
               null
           }
        } else {
            null
        }
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
