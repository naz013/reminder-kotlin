package com.github.naz013.ui.common.compose.foundation.component

/**
 * A single action button spec for a "main action + secondary actions" screen, e.g. a
 * notification action screen (reminder, birthday).
 */
data class ScreenActionItem<T>(
  val action: T,
  val text: String,
  val iconRes: Int,
)
