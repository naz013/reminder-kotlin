package com.elementary.tasks.core.views.recyclerview

import android.graphics.Rect
import android.view.View

import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(
  private val space: Int
) : RecyclerView.ItemDecoration() {

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    outRect.top = space
    outRect.left = space
    outRect.right = space
    parent.adapter?.itemCount?.takeIf { it > 0 }?.also {
      if (it - 1 == parent.getChildAdapterPosition(view)) {
        outRect.bottom = space
      }
    }
  }
}
