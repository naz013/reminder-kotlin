package com.elementary.tasks.navigation.settings.additional

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import kotlinx.android.synthetic.main.list_item_message.view.*
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
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_message, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: SmsTemplate) {
            itemView.messageView.text = item.title
        }

        init {
            itemView.buttonMore.visibility = View.VISIBLE
            itemView.clickView.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
            }
            itemView.buttonMore.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.MORE)
            }
        }
    }
}
