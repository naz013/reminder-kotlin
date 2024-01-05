package com.elementary.tasks.reminder.build.valuedialog

import com.elementary.tasks.reminder.build.BuilderItem

interface ValueDialogCallback {
  fun onValueChanged(position: Int, builderItem: BuilderItem<*>)
}
