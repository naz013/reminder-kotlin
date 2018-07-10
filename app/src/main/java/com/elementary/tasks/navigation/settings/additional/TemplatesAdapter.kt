package com.elementary.tasks.navigation.settings.additional

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
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
internal class TemplatesAdapter : RecyclerView.Adapter<TemplatesAdapter.ViewHolder>() {

    var data: List<SmsTemplate> = ArrayList()
        set(list) {
            field = list
            notifyDataSetChanged()
        }
    private var actionsListener: ActionsListener<SmsTemplate>? = null
        set

    override fun getItemCount(): Int {
        return data.size
    }

    fun getItem(position: Int): SmsTemplate {
        return data[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding!!.item = getItem(position)
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ListItemMessageBinding?

        init {
            binding = DataBindingUtil.bind(itemView)
            binding!!.root.setOnClickListener { view -> openTemplate(view, adapterPosition) }
            binding.root.setOnLongClickListener { view ->
                if (actionsListener != null) {
                    actionsListener!!.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.MORE)
                }
                true
            }
        }
    }

    private fun openTemplate(view: View, position: Int) {
        if (actionsListener != null) {
            actionsListener!!.onAction(view, position, getItem(position), ListActions.OPEN)
        }
    }
}
