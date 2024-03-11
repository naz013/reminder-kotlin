package com.elementary.tasks.reminder.build.valuedialog.controller

import androidx.annotation.DrawableRes
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.ValueDialogState

interface ValueControllerParent {
  fun onValueChanged(builderItem: BuilderItem<*>)
  fun getState(): ValueDialogState
  fun addOptionalButton(@DrawableRes icon: Int)
}
