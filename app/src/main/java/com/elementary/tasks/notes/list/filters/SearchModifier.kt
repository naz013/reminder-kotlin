package com.elementary.tasks.notes.list.filters

import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.filter.Modifier

class SearchModifier(
  modifier: Modifier<UiNoteList>? = null,
  callback: ((List<UiNoteList>) -> Unit)? = null
) : Modifier<UiNoteList>(modifier, callback) {

  private var searchValue: String = ""

  override fun apply(data: List<UiNoteList>): List<UiNoteList> {
    val list = mutableListOf<UiNoteList>()
    for (note in super.apply(data)) {
      if (filter(note)) list.add(note)
    }
    return list
  }

  private fun filter(v: UiNoteList): Boolean {
    return searchValue.isEmpty() || v.text.lowercase().contains(searchValue.lowercase())
  }

  fun setSearchValue(value: String?) {
    searchValue = value ?: ""
    onChanged()
  }
}