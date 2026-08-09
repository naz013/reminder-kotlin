package com.github.naz013.insights

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface InsightsNavKey : NavKey {
  @Serializable
  data object Dashboard : InsightsNavKey
}
