package com.elementary.tasks.reminder.build.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.reminder.build.UiBuilderItem
import com.elementary.tasks.reminder.build.UiListBuilderItem

class BuilderAdapter(
  private val onItemClickListener: (Int, UiListBuilderItem) -> Unit,
  private val onItemRemove: (Int, UiListBuilderItem) -> Unit
) : ListAdapter<UiBuilderItem, RecyclerView.ViewHolder>(
  UiBuilderItemDiffCallback()
) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return BuilderViewHolder(parent, {
      onItemClickListener(it, getItem(it) as UiListBuilderItem)
    }, {
      onItemRemove(it, getItem(it) as UiListBuilderItem)
    })
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is BuilderViewHolder -> {
        holder.bind(getItem(position) as UiListBuilderItem)
      }
    }
  }
}
