package com.elementary.tasks.notes.list.filters

import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.filter.Modifier

class SortModifier(
  modifier: Modifier<NoteWithImages>? = null,
  callback: ((List<NoteWithImages>) -> Unit)? = null
) : Modifier<NoteWithImages>(modifier, callback) {

  private var order: String = DATE_ZA

  override fun apply(data: List<NoteWithImages>): List<NoteWithImages> {
    return when (order) {
      DATE_AZ -> sortDateAz(super.apply(data))
      TEXT_AZ -> sortNameAz(super.apply(data))
      TEXT_ZA -> sortNameZa(super.apply(data))
      else -> sortDateZa(super.apply(data))
    }
  }

  private fun sortNameAz(data: List<NoteWithImages>): List<NoteWithImages> {
    return data.sortedBy { it.getSummary() }
  }

  private fun sortNameZa(data: List<NoteWithImages>): List<NoteWithImages> {
    return data.sortedByDescending { it.getSummary() }
  }

  private fun sortDateAz(data: List<NoteWithImages>): List<NoteWithImages> {
    return data.sortedBy { it.getGmtTime() }
  }

  private fun sortDateZa(data: List<NoteWithImages>): List<NoteWithImages> {
    return data.sortedByDescending { it.getGmtTime() }
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