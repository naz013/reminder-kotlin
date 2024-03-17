package com.elementary.tasks.core.views.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class StaggeredSpaceItemDecoration(
  private val spanCount: Int,
  private val spacing: Int,
  private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    val position = parent.getChildAdapterPosition(view)
    val lp = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
    val column = lp.spanIndex

    if (includeEdge) {
      // spacing - column * ((1f / spanCount) * spacing)
      outRect.left = spacing - column * spacing / spanCount

      // (column + 1) * ((1f / spanCount) * spacing)
      outRect.right = (column + 1) * spacing / spanCount

      if (position < spanCount) {
        outRect.top = spacing
      }
      outRect.bottom = spacing
    } else {
      // column * ((1f / spanCount) * spacing)
      outRect.left = column * spacing / spanCount

      // spacing - (column + 1) * ((1f /    spanCount) * spacing)
      outRect.right = spacing - (column + 1) * spacing / spanCount

      if (position >= spanCount) {
        outRect.top = spacing
      }
    }
  }
}
