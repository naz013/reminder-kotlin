package com.elementary.tasks.core.interfaces

import android.view.View

import com.elementary.tasks.core.utils.ListActions

interface ActionsListener<T> {
  fun onAction(view: View, position: Int, t: T?, actions: ListActions)
}
