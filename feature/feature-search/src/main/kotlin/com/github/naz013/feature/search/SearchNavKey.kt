package com.github.naz013.feature.search

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface SearchNavKey : NavKey {
  @Serializable
  data object Main : SearchNavKey
}
