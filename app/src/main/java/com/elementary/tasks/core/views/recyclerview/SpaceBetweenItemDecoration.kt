package com.elementary.tasks.core.views.recyclerview

import android.graphics.Rect
import android.view.View

import androidx.recyclerview.widget.RecyclerView

class SpaceBetweenItemDecoration(
  private val space: Int
) : RecyclerView.ItemDecoration() {

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    // Add Top space to each next list item
    if (parent.getChildAdapterPosition(view) > 0) {
      outRect.top = space
    }
  }
}
