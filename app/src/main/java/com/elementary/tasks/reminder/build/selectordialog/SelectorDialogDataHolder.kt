package com.elementary.tasks.reminder.build.selectordialog

import androidx.compose.runtime.Composable
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.reminder.build.UiSelectorItem
import org.koin.compose.koinInject

class SelectorDialogDataHolder {
  var selectorBuilderItems: List<UiSelectorItem> = emptyList()
  var presets: List<UiPresetList> = emptyList()
  var recurPresets: List<UiPresetList> = emptyList()

  fun getTabs(): List<SelectorTab> =
    listOfNotNull(
      SelectorTab.BUILDER,
      SelectorTab.PRESETS.takeIf { presets.isNotEmpty() },
      SelectorTab.RECUR_PRESETS.takeIf { BuildParams.isPro && recurPresets.isNotEmpty() },
    )
}

@Composable
fun rememberSelectorDialogDataHolder(): SelectorDialogDataHolder = koinInject()
