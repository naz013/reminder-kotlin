package com.elementary.tasks.core.app_widgets.singlenote

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.data.ui.note.UiNoteListSelectable
import timber.log.Timber

class SelectableNotesRecyclerAdapter(
  private val onSelectionChangeListener: (id: String) -> Unit
) : ListAdapter<UiNoteListSelectable, SelectableNoteViewHolder>(
  UiNoteListSelectableDiffCallback()
) {

  private var isAutoSelectUsed = false

  fun autoSelectId(id: String) {
    if (isAutoSelectUsed) {
      return
    }
    isAutoSelectUsed = true
    val index =  currentList.indexOfFirst { it.id == id }
    if (index != -1) {
      updateSelection(index)
    }
  }

  fun getSelectedId(): String? {
    return currentList.firstOrNull { it.isSelected }?.id
  }

  private fun updateSelection(position: Int) {
    val item = getItem(position)
    Timber.d("updateSelection: position=$position, isSelected=${item.isSelected}")
    if (item.isSelected) {
      return
    }
    val index = findSelectedPosition()
    if (index != -1) {
      currentList[index].isSelected = false
      notifyItemChanged(index)
    }
    Timber.d("updateSelection: index=$index")
    currentList[position].isSelected = true
    notifyItemChanged(position)
    onSelectionChangeListener.invoke(currentList[position].id)
  }

  private fun findSelectedPosition(): Int {
    return currentList.indexOfFirst { it.isSelected }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableNoteViewHolder {
    return SelectableNoteViewHolder(parent) { updateSelection(it) }
  }

  override fun onBindViewHolder(holder: SelectableNoteViewHolder, position: Int) {
    holder.setData(getItem(position))
  }
}
