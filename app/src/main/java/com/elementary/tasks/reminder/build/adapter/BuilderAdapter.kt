package com.elementary.tasks.reminder.build.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.reminder.build.UiBuilderItem
import com.elementary.tasks.reminder.build.UiListBuilderItem
import com.elementary.tasks.reminder.build.UiListNoteBuilderItem
import com.elementary.tasks.reminder.build.adapter.viewholder.BaseBuilderViewHolder
import com.elementary.tasks.reminder.build.adapter.viewholder.BuilderAdapterViewType
import com.elementary.tasks.reminder.build.adapter.viewholder.BuilderNoteViewHolder
import com.elementary.tasks.reminder.build.adapter.viewholder.BuilderViewHolder

class BuilderAdapter(
  private val onItemClickListener: (Int, UiBuilderItem) -> Unit,
  private val onItemRemove: (Int, UiBuilderItem) -> Unit
) : ListAdapter<UiBuilderItem, BaseBuilderViewHolder<*, *>>(
  UiBuilderItemDiffCallback()
) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBuilderViewHolder<*, *> {
    val onClick: (Int) -> Unit = {
      onItemClickListener(it, getItem(it) as UiBuilderItem)
    }
    val onRemove: (Int) -> Unit = {
      onItemRemove(it, getItem(it) as UiBuilderItem)
    }
    return when (viewType) {
      BuilderAdapterViewType.NOTE.value -> {
        BuilderNoteViewHolder(parent, onClick, onRemove)
      }
      else -> BuilderViewHolder(parent, onClick, onRemove)
    }
  }

  override fun onBindViewHolder(holder: BaseBuilderViewHolder<*, *>, position: Int) {
    when (holder) {
      is BuilderViewHolder -> {
        holder.bind(getItem(position) as UiListBuilderItem)
      }
      is BuilderNoteViewHolder -> {
        holder.bind(getItem(position) as UiListNoteBuilderItem)
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    val item = getItem(position)
    return when {
      item is UiListNoteBuilderItem && item.noteData != null -> BuilderAdapterViewType.NOTE.value
      else -> BuilderAdapterViewType.ITEM.value
    }
  }
}
