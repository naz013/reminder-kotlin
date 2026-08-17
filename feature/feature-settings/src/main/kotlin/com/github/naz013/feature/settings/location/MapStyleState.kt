package com.github.naz013.feature.settings.location

import androidx.annotation.StringRes

data class MapStyleState(
  val options: List<MapStyleOption> = emptyList(),
  val selectedIndex: Int = 0,
)

data class MapStyleOption(
  val index: Int,
  @StringRes val titleRes: Int,
  val previews: List<Int>,
)
