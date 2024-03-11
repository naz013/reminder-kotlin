package com.elementary.tasks.reminder.build.valuedialog.controller

import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.reminder.build.BuilderItem

interface ValueController {
  fun getView(parent: ViewGroup): View
  fun onViewAdded(parent: ValueControllerParent)
  fun clearValue()
  fun putValues()
  fun cancelChanges()
  fun getItem(): BuilderItem<*>
  fun isDraggable(): Boolean
  fun onStop()
  fun onDestroy()
  fun onOptionalClicked()
}
