package com.github.naz013.feature.agenda

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AgendaNavKey : NavKey {
  @Serializable
  data object List : AgendaNavKey
}
