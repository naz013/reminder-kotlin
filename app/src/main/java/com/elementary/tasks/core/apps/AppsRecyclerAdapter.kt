package com.elementary.tasks.core.apps

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import kotlinx.android.synthetic.main.list_item_application.view.*

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
        return ApplicationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_application, parent, false))
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ApplicationItem) {
            itemView.itemName.text = item.name
            loadImage(itemView.itemImage, item.drawable)
        }

        init {
            itemView.clickView.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
            }
        }
    }

    fun loadImage(imageView: ImageView, v: Drawable?) {
        imageView.setImageDrawable(v)
    }
}
