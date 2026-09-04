package com.github.naz013.feature.settings.search

import androidx.navigation3.runtime.NavKey

/** A [SettingsSearchEntry] with its title resolved for display, ready to render as a search result row. */
internal data class SettingsSearchResult(
  val title: String,
  val path: List<NavKey>,
  val highlightItemId: String?,
)
