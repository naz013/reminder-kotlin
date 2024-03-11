package com.elementary.tasks.reminder.build.selectordialog

import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.reminder.build.BuilderItem

interface SelectorDialogCallback {
  fun onBuilderItemAdd(builderItem: BuilderItem<*>)
  fun onPresetSelected(uiPresetList: UiPresetList)
}
