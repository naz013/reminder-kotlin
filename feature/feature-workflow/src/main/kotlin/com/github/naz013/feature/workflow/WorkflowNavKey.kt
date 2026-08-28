package com.github.naz013.feature.workflow

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface WorkflowNavKey : NavKey {
  @Serializable
  data object Gallery : WorkflowNavKey

  @Serializable
  data class RulesForGroup(
    val groupId: String
  ) : WorkflowNavKey

  @Serializable
  data class RulesForReminder(
    val reminderId: String
  ) : WorkflowNavKey

  /** [scopeType] is a [com.github.naz013.domain.workflow.WorkflowScopeType] name - kept as a
   * primitive here since Nav3 keys need to stay simple/serializable, not a raw sealed
   * `WorkflowScope`. [scopeId] is the group/reminder id for non-global scopes.
   * [editingRuleId] is non-null when editing an existing rule instead of creating one. */
  @Serializable
  data class Builder(
    val scopeType: String,
    val scopeId: String? = null,
    val editingRuleId: String? = null
  ) : WorkflowNavKey
}
