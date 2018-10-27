package com.elementary.tasks.notes.editor.layers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.drawing.Background
import com.elementary.tasks.core.drawing.Drawing
import com.elementary.tasks.core.drawing.Image
import com.elementary.tasks.core.drawing.Text
import com.elementary.tasks.core.interfaces.Observer
import kotlinx.android.synthetic.main.list_item_layer.view.*
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
class LayersRecyclerAdapter : RecyclerView.Adapter<LayersRecyclerAdapter.ViewHolder>(), Observer {

    private val mDataList: MutableList<Drawing> = mutableListOf()
    var onStartDragListener: OnStartDragListener? = null
    var mCallback: AdapterCallback? = null

    private var index: Int = 0

    fun setIndex(index: Int) {
        this.index = index
        notifyDataSetChanged()
    }

    fun getIndex(): Int {
        return index
    }

    internal fun onItemDismiss(position: Int) {
        mDataList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, mDataList.size)
        mCallback?.onItemRemoved(position)
    }

    internal fun onItemMove(from: Int, to: Int) {
        Collections.swap(mDataList, from, to)
        notifyItemMoved(from, to)
        mCallback?.onChanged()
        notifyDataSetChanged()
    }

    override fun setUpdate(o: Any) {
        if (o is Int) {
            this.index = o - 1
        }
        notifyDataSetChanged()
        mCallback?.onItemAdded()
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        fun bind(drawing: Drawing) {
            itemView.layerName.text = getName(itemView.context, drawing)
            itemView.layerView.drawing = drawing
            if (adapterPosition == index) {
                itemView.selectionView.setBackgroundResource(R.color.redPrimary)
            } else {
                itemView.selectionView.setBackgroundResource(android.R.color.transparent)
            }
        }

        init {
            itemView.setOnClickListener {
                mCallback?.onItemSelect(adapterPosition)
            }
            itemView.setOnLongClickListener {
                onStartDragListener?.onStartDrag(this)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayersRecyclerAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_layer, parent, false))
    }

    override fun onBindViewHolder(holder: LayersRecyclerAdapter.ViewHolder, position: Int) {
        holder.bind(mDataList[position])
    }

    private fun getName(context: Context, drawing: Drawing): String {
        return when (drawing) {
            is Background -> context.getString(R.string.background)
            is Image -> context.getString(R.string.image)
            is Text -> drawing.text
            else -> context.getString(R.string.figure)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    fun setData(elements: ArrayList<Drawing>) {
        this.mDataList.clear()
        this.mDataList.addAll(elements)
        notifyDataSetChanged()
    }

    interface AdapterCallback {
        fun onChanged()

        fun onItemSelect(position: Int)

        fun onItemRemoved(position: Int)

        fun onItemAdded()
    }
}
