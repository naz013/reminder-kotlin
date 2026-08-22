package com.github.naz013.feature.settings.headeritems

import com.github.naz013.domain.home.HeaderNavigationSection

internal data class HeaderItemsSettingsState(
  val pinnedItems: List<HeaderItemRow> = emptyList(),
  val configurableItems: List<HeaderItemRow> = emptyList(),
)

internal data class HeaderItemRow(
  val section: HeaderNavigationSection,
  val titleRes: Int,
  val iconRes: Int,
  val isEnabled: Boolean,
)
