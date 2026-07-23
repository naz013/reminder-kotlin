package com.elementary.tasks.workflow

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface WorkflowNavKey : NavKey {
  @Serializable
  data object Gallery : WorkflowNavKey

  @Serializable
  data class RulesForGroup(
    val groupId: String
  ) : WorkflowNavKey
}
