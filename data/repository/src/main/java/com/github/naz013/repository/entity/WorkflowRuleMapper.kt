package com.github.naz013.repository.entity

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope

internal fun WorkflowRule.toEntity(): WorkflowRuleEntity {
  val (scopeType, scopeId) = scope.toColumns()
  val (triggerType, triggerPayload) = trigger.toColumns()
  val (actionType, actionPayload) = action.toColumns()
  return WorkflowRuleEntity(
    uuId = uuId,
    title = title,
    templateId = templateId,
    scopeType = scopeType,
    scopeId = scopeId,
    triggerType = triggerType,
    triggerPayload = triggerPayload,
    conditionsPayload = conditions.toConditionsPayload(),
    actionType = actionType,
    actionPayload = actionPayload,
    isEnabled = isEnabled,
    createdAt = createdAt.toEpochMillisUtc(),
    lastRunAt = lastRunAt?.toEpochMillisUtc(),
    version = version,
    syncState = syncState.name
  )
}

internal fun WorkflowRuleEntity.toDomain(): WorkflowRule = WorkflowRule(
  uuId = uuId,
  title = title,
  templateId = templateId,
  scope = toWorkflowScope(scopeType, scopeId),
  trigger = toWorkflowTrigger(triggerType, triggerPayload),
  conditions = conditionsPayload.toWorkflowConditions(),
  action = toWorkflowAction(actionType, actionPayload),
  isEnabled = isEnabled,
  createdAt = createdAt.toLocalDateTimeUtc(),
  lastRunAt = lastRunAt?.toLocalDateTimeUtc(),
  version = version,
  syncState = SyncState.valueOf(syncState)
)

private fun WorkflowScope.toColumns(): Pair<String, String?> = when (this) {
  is WorkflowScope.Global -> "GLOBAL" to null
  is WorkflowScope.ForGroup -> "GROUP" to groupId
  is WorkflowScope.ForReminder -> "REMINDER" to reminderId
}

private fun toWorkflowScope(scopeType: String, scopeId: String?): WorkflowScope = when (scopeType) {
  "GROUP" -> scopeId?.let { WorkflowScope.ForGroup(it) } ?: WorkflowScope.Global
  "REMINDER" -> scopeId?.let { WorkflowScope.ForReminder(it) } ?: WorkflowScope.Global
  else -> WorkflowScope.Global
}
