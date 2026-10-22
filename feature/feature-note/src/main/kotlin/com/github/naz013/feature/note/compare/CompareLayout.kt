package com.github.naz013.feature.note.compare

import com.github.naz013.ui.common.compose.foundation.DeviceScreenConfiguration

internal enum class CompareLayout {
  Stacked,
  SideBySide,
}

/** Phones in portrait stack the panes vertically; every wider window class puts them side by side. */
internal fun compareLayoutFor(configuration: DeviceScreenConfiguration): CompareLayout =
  when (configuration) {
    DeviceScreenConfiguration.MobilePortrait -> CompareLayout.Stacked
    else -> CompareLayout.SideBySide
  }
