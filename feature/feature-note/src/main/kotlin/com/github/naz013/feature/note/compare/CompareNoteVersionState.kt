package com.github.naz013.feature.note.compare

internal enum class CompareStatus {
  Loading,
  Loaded,
  Unavailable,
}

internal data class CompareNoteVersionState(
  val status: CompareStatus = CompareStatus.Loading,
  val historyLabel: String = "",
  val historyLines: List<DiffLine> = emptyList(),
  val currentLines: List<DiffLine> = emptyList(),
  val isIdentical: Boolean = false,
)
