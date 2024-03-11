package com.elementary.tasks.reminder.build.selectordialog.params

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.reminder.build.UiSelectorItem

class SelectorAdapter(
  private val onItemClickListener: (Int, UiSelectorItem) -> Unit
) : ListAdapter<UiSelectorItem, SelectorViewHolder>(
  UiSelectorItemDiffCallback()
) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectorViewHolder {
    return SelectorViewHolder(parent, {
      onItemClickListener(it, getItem(it))
    })
  }

  override fun onBindViewHolder(holder: SelectorViewHolder, position: Int) {
    holder.bind(getItem(position))
  }
}
