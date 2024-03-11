package com.elementary.tasks.reminder.build.selectordialog

import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.reminder.build.UiSelectorItem

class SelectorDialogDataHolder {

  var selectorBuilderItems: List<UiSelectorItem> = emptyList()
  var presets: List<UiPresetList> = emptyList()
  var recurPresets: List<UiPresetList> = emptyList()

  fun getTabs(): List<SelectorTab> {
    return listOfNotNull(
      SelectorTab.BUILDER,
      SelectorTab.PRESETS.takeIf { presets.isNotEmpty() },
      SelectorTab.RECUR_PRESETS.takeIf { Module.isPro && recurPresets.isNotEmpty() }
    )
  }
}
