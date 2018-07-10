package com.elementary.tasks.places.google

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.databinding.ListItemLocationBinding

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
class LocationPlacesAdapter : RecyclerView.Adapter<LocationPlacesAdapter.ViewHolder>() {

    private val mDataList = ArrayList<Reminder>()
    var actionsListener: ActionsListener<Reminder>? = null

    fun setData(list: List<Reminder>) {
        this.mDataList.clear()
        this.mDataList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        internal var binding: ListItemLocationBinding? = null

        init {
            binding = DataBindingUtil.bind(v)
            v.setOnClickListener { view ->
                if (actionsListener != null) {
                    actionsListener!!.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
                }
            }
            v.setOnLongClickListener { view ->
                if (actionsListener != null) {
                    actionsListener!!.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.MORE)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDataList[position]
        val place = item.places[0]
        var name = place.name
        if (item.places.size > 1) {
            name = item.summary + " (" + item.places.size + ")"
        }
        holder.binding!!.item = place
        holder.binding!!.name = name
    }

    fun getItem(position: Int): Reminder {
        return mDataList[position]
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }
}
