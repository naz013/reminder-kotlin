package com.elementary.tasks.reminder.create.fragments.recur.preview

data class PreviewData(
  val scrollTo: Int,
  val items: List<PreviewItem>
)

data class PreviewItem(
  val text: String,
  val style: Style
)

enum class Style {
  DISABLED,
  BOLD,
  NORMAL
}
