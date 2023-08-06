package com.elementary.tasks.notes.list

import com.elementary.tasks.core.data.ui.note.UiNoteList

class NoteSortProcessor {

  fun apply(data: List<UiNoteList>, order: String): List<UiNoteList> {
    return when (order) {
      DATE_AZ -> sortDateAz(data)
      TEXT_AZ -> sortNameAz(data)
      TEXT_ZA -> sortNameZa(data)
      else -> sortDateZa(data)
    }
  }

  private fun sortNameAz(data: List<UiNoteList>): List<UiNoteList> {
    return data.sortedBy { it.text }
  }

  private fun sortNameZa(data: List<UiNoteList>): List<UiNoteList> {
    return data.sortedByDescending { it.text }
  }

  private fun sortDateAz(data: List<UiNoteList>): List<UiNoteList> {
    return data.sortedBy { it.formattedDateTime }
  }

  private fun sortDateZa(data: List<UiNoteList>): List<UiNoteList> {
    return data.sortedByDescending { it.formattedDateTime }
  }

  companion object {
    const val DATE_AZ = "date_az"
    const val DATE_ZA = "date_za"
    const val TEXT_AZ = "text_az"
    const val TEXT_ZA = "text_za"
  }
}
