package com.github.naz013.feature.reminder.lists.removed

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface RemindersArchiveNavKey : NavKey {
  @Serializable
  data object List : RemindersArchiveNavKey
}
