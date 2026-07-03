package com.elementary.tasks.notes.list

class NoteSortProcessor {

  fun apply(data: List<UiNoteListItem>, order: String): List<UiNoteListItem> {
    return when (order) {
      DATE_AZ -> sortDateAz(data)
      TEXT_AZ -> sortNameAz(data)
      TEXT_ZA -> sortNameZa(data)
      else -> sortDateZa(data)
    }
  }

  private fun sortNameAz(data: List<UiNoteListItem>): List<UiNoteListItem> {
    return data.sortedBy { it.text }
  }

  private fun sortNameZa(data: List<UiNoteListItem>): List<UiNoteListItem> {
    return data.sortedByDescending { it.text }
  }

  private fun sortDateAz(data: List<UiNoteListItem>): List<UiNoteListItem> {
    return data.sortedBy { it.formattedDateTime }
  }

  private fun sortDateZa(data: List<UiNoteListItem>): List<UiNoteListItem> {
    return data.sortedByDescending { it.formattedDateTime }
  }

  companion object {
    const val DATE_AZ = "date_az"
    const val DATE_ZA = "date_za"
    const val TEXT_AZ = "text_az"
    const val TEXT_ZA = "text_za"
  }
}
