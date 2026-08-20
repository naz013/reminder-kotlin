package com.github.naz013.feature.settings.location

import androidx.annotation.StringRes

internal data class MapStyleState(
  val options: List<MapStyleOption> = emptyList(),
  val selectedIndex: Int = 0,
)

internal data class MapStyleOption(
  val index: Int,
  @StringRes val titleRes: Int,
  val previews: List<Int>,
)
