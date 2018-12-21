package com.elementary.tasks.places.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ThemeUtil
import kotlinx.android.synthetic.main.list_item_place.view.*

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
class PlacesRecyclerAdapter : ListAdapter<Place, PlacesRecyclerAdapter.ViewHolder>(PlaceDiffCallback()) {

    var actionsListener: ActionsListener<Place>? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Place) {
            itemView.textView.text = item.name
            itemView.markerImage.setImageResource(ThemeUtil.getMarkerStyle(item.marker))
        }

        init {
            itemView.itemCard.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
            }
            itemView.buttonMore.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.MORE)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_place, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
