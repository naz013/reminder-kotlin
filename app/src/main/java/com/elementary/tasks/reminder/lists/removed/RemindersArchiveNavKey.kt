package com.elementary.tasks.reminder.lists.removed

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface RemindersArchiveNavKey : NavKey {
  @Serializable
  data object List : RemindersArchiveNavKey
}
