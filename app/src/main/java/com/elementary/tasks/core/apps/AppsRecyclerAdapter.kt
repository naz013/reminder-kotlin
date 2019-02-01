package com.elementary.tasks.core.apps

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.databinding.ListItemApplicationBinding

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
class AppsRecyclerAdapter : ListAdapter<ApplicationItem, AppsRecyclerAdapter.ApplicationViewHolder>(AppsDiffCallback()) {

    var actionsListener: ActionsListener<ApplicationItem>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        return ApplicationViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ApplicationViewHolder(parent: ViewGroup) : HolderBinding<ListItemApplicationBinding>(parent, R.layout.list_item_application) {
        fun bind(item: ApplicationItem) {
            binding.itemName.text = item.name
            loadImage(binding.itemImage, item.drawable)
        }

        init {
            binding.clickView.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
            }
        }
    }

    private fun loadImage(imageView: ImageView, v: Drawable?) {
        imageView.setImageDrawable(v)
    }
}
