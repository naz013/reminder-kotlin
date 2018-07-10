package com.elementary.tasks.places.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemPlaceBinding

import java.util.ArrayList
import androidx.databinding.BindingAdapter
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
class PlacesRecyclerAdapter : RecyclerView.Adapter<PlacesRecyclerAdapter.ViewHolder>() {

    private val mData = ArrayList<Place>()
    var actionsListener: ActionsListener<Place>? = null

    var data: List<Place>
        get() = mData
        set(list) {
            this.mData.clear()
            this.mData.addAll(list)
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        internal var binding: ListItemPlaceBinding? = null

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

    fun getItem(position: Int): Place {
        return mData[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding!!.item = getItem(position)
    }

    companion object {

        @BindingAdapter("loadMarker")
        fun loadMarker(view: ImageView, color: Int) {
            view.setImageResource(ThemeUtil.getInstance(view.context).getMarkerStyle(color))
        }
    }
}
