package com.elementary.tasks.notes.list.filters

import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.filter.Modifier

class SortModifier(
  modifier: Modifier<UiNoteList>? = null,
  callback: ((List<UiNoteList>) -> Unit)? = null
) : Modifier<UiNoteList>(modifier, callback) {

  private var order: String = DATE_ZA

  override fun apply(data: List<UiNoteList>): List<UiNoteList> {
    return when (order) {
      DATE_AZ -> sortDateAz(super.apply(data))
      TEXT_AZ -> sortNameAz(super.apply(data))
      TEXT_ZA -> sortNameZa(super.apply(data))
      else -> sortDateZa(super.apply(data))
    }
  }

  private fun sortNameAz(data: List<UiNoteList>): List<UiNoteList> {
    return data.sortedBy { it.text }
  }

  private fun sortNameZa(data: List<UiNoteList>): List<UiNoteList> {
    return data.sortedByDescending { it.text }
  }

  private fun sortDateAz(data: List<UiNoteList>): List<UiNoteList> {
    return data.sortedBy { it.formattedDateTime}
  }

  private fun sortDateZa(data: List<UiNoteList>): List<UiNoteList> {
    return data.sortedByDescending { it.formattedDateTime }
  }

  fun setOrder(value: String?) {
    order = value ?: DATE_ZA
    onChanged()
  }

  companion object {
    const val DATE_AZ = "date_az"
    const val DATE_ZA = "date_za"
    const val TEXT_AZ = "text_az"
    const val TEXT_ZA = "text_za"
  }
}