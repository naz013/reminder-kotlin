package com.elementary.tasks.places.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.DrawableHelper
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.databinding.ListItemPlaceBinding

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

    inner class ViewHolder(parent: ViewGroup) : BaseHolder<ListItemPlaceBinding>(parent, R.layout.list_item_place) {
        fun bind(item: Place) {
            binding.textView.text = item.name
            DrawableHelper.withContext(itemView.context)
                    .withDrawable(R.drawable.ic_twotone_place_24px)
                    .withColor(themeUtil.getNoteLightColor(item.marker))
                    .tint()
                    .applyTo(binding.markerImage)
        }

        init {
            binding.itemCard.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
            }
            binding.buttonMore.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.MORE)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
