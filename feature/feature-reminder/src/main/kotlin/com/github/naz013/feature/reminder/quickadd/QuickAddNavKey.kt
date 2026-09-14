package com.github.naz013.feature.reminder.quickadd

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface QuickAddNavKey : NavKey {
  @Serializable
  data object Sheet : QuickAddNavKey
}
