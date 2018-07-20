package com.elementary.tasks.notes.editor.layers

import android.graphics.Canvas
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Copyright 2017 Nazar Suhovich
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
class SimpleItemTouchHelperCallback(private val mAdapter: LayersRecyclerAdapter) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return if (recyclerView.layoutManager is GridLayoutManager) {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
        } else {
            val dragFlags = ItemTouchHelper.START or ItemTouchHelper.END
            val swipeFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
        }
    }

    override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        if (source.itemViewType != target.itemViewType) {
            return false
        }
        mAdapter.onItemMove(source.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
        mAdapter.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val alpha = ALPHA_FULL - Math.abs(dY) / viewHolder.itemView.width.toFloat()
            viewHolder.itemView.alpha = alpha
            viewHolder.itemView.translationY = dY
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.alpha = ALPHA_FULL
    }

    companion object {
        const val ALPHA_FULL = 1.0f
    }
}
