package com.github.naz013.feature.reminder.build.selectordialog

import androidx.compose.runtime.Composable
import com.github.naz013.feature.reminder.preset.UiPresetList
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.feature.reminder.build.UiSelectorItem
import org.koin.compose.koinInject

internal class SelectorDialogDataHolder(
  private val buildInfo: BuildInfo,
) {
  var selectorBuilderItems: List<UiSelectorItem> = emptyList()
  var presets: List<UiPresetList> = emptyList()
  var recurPresets: List<UiPresetList> = emptyList()

  fun getTabs(): List<SelectorTab> =
    listOfNotNull(
      SelectorTab.BUILDER,
      SelectorTab.PRESETS.takeIf { presets.isNotEmpty() },
      SelectorTab.RECUR_PRESETS.takeIf { buildInfo.isPro && recurPresets.isNotEmpty() },
    )
}

@Composable
internal fun rememberSelectorDialogDataHolder(): SelectorDialogDataHolder = koinInject()
