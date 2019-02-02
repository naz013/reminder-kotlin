package com.elementary.tasks.navigation.settings.additional

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.databinding.ListItemMessageBinding
import java.util.*

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
internal class TemplatesAdapter : RecyclerView.Adapter<TemplatesAdapter.ViewHolder>() {

    var data: List<SmsTemplate> = ArrayList()
        set(list) {
            field = list
            notifyDataSetChanged()
        }
    var actionsListener: ActionsListener<SmsTemplate>? = null

    override fun getItemCount(): Int {
        return data.size
    }

    fun getItem(position: Int): SmsTemplate {
        return data[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal inner class ViewHolder(parent: ViewGroup) : HolderBinding<ListItemMessageBinding>(parent, R.layout.list_item_message) {
        fun bind(item: SmsTemplate) {
            binding.messageView.text = item.title
        }

        init {
            binding.buttonMore.visibility = View.VISIBLE
            binding.clickView.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
            }
            binding.buttonMore.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.MORE)
            }
        }
    }
}
