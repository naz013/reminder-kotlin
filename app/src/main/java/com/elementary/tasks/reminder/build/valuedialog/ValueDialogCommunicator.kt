package com.elementary.tasks.reminder.build.valuedialog

import com.elementary.tasks.reminder.build.BuilderItem
import java.lang.ref.WeakReference

object ValueDialogCommunicator : ValueDialogCallback {

  private var callback: WeakReference<ValueDialogCallback>? = null

  fun addCallback(callback: ValueDialogCallback) {
    this.callback = WeakReference(callback)
  }

  fun removeCallback() {
    this.callback = null
  }

  override fun onValueChanged(position: Int, builderItem: BuilderItem<*>) {
    callback?.get()?.onValueChanged(position, builderItem)
  }
}
