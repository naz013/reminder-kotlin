package com.elementary.tasks.notes.create.images

import android.content.Context

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class KeepLayoutManager(
  context: Context,
  spanCount: Int,
  private val mAdapter: RecyclerView.Adapter<*>
) : GridLayoutManager(context, spanCount) {

  init {
    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        val size = mAdapter.itemCount
        return when (size % 3) {
          1 -> if (position == 0) {
            6
          } else {
            2
          }
          2 -> if (position < 2) {
            3
          } else {
            2
          }
          else -> 2
        }
      }
    }
  }
}
