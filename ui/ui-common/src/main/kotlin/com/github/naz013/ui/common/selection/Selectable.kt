package com.github.naz013.ui.common.selection

/**
 * Implemented by list-item UI models that support the app's multiselect pattern (see
 * `docs/multiselect.md`). [id] must be stable and unique within the list it's rendered in;
 * [withSelected] should be a cheap `copy(isSelected = selected)`.
 */
interface Selectable<T> {
  val id: String
  val isSelected: Boolean

  fun withSelected(selected: Boolean): T
}

fun <T : Selectable<T>> List<T>.select(id: String): List<T> =
  map { if (it.id == id) it.withSelected(true) else it }

fun <T : Selectable<T>> List<T>.toggleSelection(id: String): List<T> =
  map { if (it.id == id) it.withSelected(!it.isSelected) else it }

fun <T : Selectable<T>> List<T>.clearSelection(): List<T> =
  map { if (it.isSelected) it.withSelected(false) else it }

fun <T : Selectable<T>> List<T>.selectedCount(): Int = count { it.isSelected }

fun <T : Selectable<T>> List<T>.selectedIds(): Set<String> =
  filter { it.isSelected }.mapTo(mutableSetOf()) { it.id }
