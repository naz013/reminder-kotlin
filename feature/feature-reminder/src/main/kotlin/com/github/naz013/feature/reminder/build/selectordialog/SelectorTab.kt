package com.github.naz013.feature.reminder.build.selectordialog

import androidx.annotation.StringRes
import com.github.naz013.ui.common.R

enum class SelectorTab(
  @StringRes val titleRes: Int,
  @StringRes val searchHintRes: Int,
) {
  BUILDER(
    R.string.builder_selector_tab_parameters,
    R.string.builder_selector_tab_search_parameters,
  ),
  PRESETS(R.string.builder_selector_tab_presets, R.string.builder_selector_tab_search_presets),
  RECUR_PRESETS(
    R.string.builder_selector_tab_recur_presets,
    R.string.builder_selector_tab_search_presets,
  ),
}
