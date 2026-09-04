package com.github.naz013.feature.settings.search

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey

/**
 * One searchable setting: a title (and optional extra keywords, e.g. "quiet hours" for
 * "Do Not Disturb") matched against the settings-search query, plus the [path] of [NavKey]s to
 * push onto the backstack to reach it - the whole parent chain, not just the leaf, so the
 * backstack after a search jump looks the same as normal drill-down navigation and the back
 * button behaves accordingly.
 *
 * [highlightItemId] is the target row's [com.github.naz013.ui.common.compose.foundation.component.SettingsItem.itemKey]
 * on the destination screen - when set, that row scrolls into view and flashes once the screen
 * lands. Leave it `null` for entries that point at a whole screen rather than one specific
 * setting inside it.
 *
 * [isProOnly] marks a setting that's either hidden entirely on a free build (e.g. marker style)
 * or shown there only behind a Pro paywall badge (e.g. Insights, AI Digest) - either way, it's
 * not something a free-build user can actually use, so it's filtered out of search results on
 * free builds rather than deep-linking them into an upsell. It stays fully searchable on `pro`.
 */
internal data class SettingsSearchEntry(
  @StringRes val titleRes: Int,
  val path: List<NavKey>,
  @StringRes val keywordRes: List<Int> = emptyList(),
  val highlightItemId: String? = null,
  val isProOnly: Boolean = false,
)
