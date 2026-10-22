package com.github.naz013.feature.note.compare

internal enum class LineChange {
  Same,
  Removed,
  Added,
}

internal data class DiffLine(
  val text: String,
  val change: LineChange,
)

/** [history] holds [LineChange.Same] and [LineChange.Removed] lines, [current] holds [LineChange.Same] and [LineChange.Added]. */
internal data class LineDiff(
  val history: List<DiffLine>,
  val current: List<DiffLine>,
) {
  val isIdentical: Boolean
    get() = history.all { it.change == LineChange.Same } && current.all { it.change == LineChange.Same }
}

/** Line-level LCS diff: lines only in [historyText] are removed, lines only in [currentText] are added. */
internal fun diffLines(historyText: String, currentText: String): LineDiff {
  val old = splitLines(historyText)
  val new = splitLines(currentText)
  val lcs = Array(old.size + 1) { IntArray(new.size + 1) }
  for (i in old.indices.reversed()) {
    for (j in new.indices.reversed()) {
      lcs[i][j] = if (old[i] == new[j]) lcs[i + 1][j + 1] + 1 else maxOf(lcs[i + 1][j], lcs[i][j + 1])
    }
  }

  val history = mutableListOf<DiffLine>()
  val current = mutableListOf<DiffLine>()
  var i = 0
  var j = 0
  while (i < old.size && j < new.size) {
    when {
      old[i] == new[j] -> {
        history += DiffLine(old[i], LineChange.Same)
        current += DiffLine(new[j], LineChange.Same)
        i++
        j++
      }

      lcs[i + 1][j] >= lcs[i][j + 1] -> history += DiffLine(old[i++], LineChange.Removed)
      else -> current += DiffLine(new[j++], LineChange.Added)
    }
  }
  while (i < old.size) history += DiffLine(old[i++], LineChange.Removed)
  while (j < new.size) current += DiffLine(new[j++], LineChange.Added)
  return LineDiff(history = history, current = current)
}

private fun splitLines(text: String): List<String> = if (text.isEmpty()) emptyList() else text.lines()
