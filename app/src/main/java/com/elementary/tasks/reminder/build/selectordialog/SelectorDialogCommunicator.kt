package com.elementary.tasks.reminder.build.selectordialog

import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.reminder.build.BuilderItem
import java.lang.ref.WeakReference

object SelectorDialogCommunicator : SelectorDialogCallback {

  private var callback: WeakReference<SelectorDialogCallback>? = null

  fun addCallback(callback: SelectorDialogCallback) {
    this.callback = WeakReference(callback)
  }

  fun removeCallback() {
    this.callback = null
  }

  override fun onBuilderItemAdd(builderItem: BuilderItem<*>) {
    callback?.get()?.onBuilderItemAdd(builderItem)
  }

  override fun onPresetSelected(uiPresetList: UiPresetList) {
    callback?.get()?.onPresetSelected(uiPresetList)
  }
}
