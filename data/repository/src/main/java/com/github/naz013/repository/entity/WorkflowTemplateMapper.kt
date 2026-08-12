package com.github.naz013.repository.entity

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory

internal fun WorkflowTemplate.toEntity(): WorkflowTemplateEntity {
  val (triggerType, triggerPayload) = trigger.toColumns()
  val (actionType, actionPayload) = action.toColumns()
  return WorkflowTemplateEntity(
    id = id,
    title = title,
    description = description,
    category = category.name,
    supportedScopeTypes = supportedScopeTypes.map { it.name },
    triggerType = triggerType,
    triggerPayload = triggerPayload,
    actionType = actionType,
    actionPayload = actionPayload,
    isBuiltIn = isBuiltIn,
    useCount = useCount,
    createdAt = createdAt.toEpochMillisUtc(),
    version = version,
    syncState = syncState.name
  )
}

internal fun WorkflowTemplateEntity.toDomain(): WorkflowTemplate = WorkflowTemplate(
  id = id,
  title = title,
  description = description,
  category = runCatching { WorkflowTemplateCategory.valueOf(category) }
    .getOrDefault(WorkflowTemplateCategory.REMINDER_LIFECYCLE),
  supportedScopeTypes = supportedScopeTypes.mapNotNull {
    runCatching { WorkflowScopeType.valueOf(it) }.getOrNull()
  },
  trigger = toWorkflowTrigger(triggerType, triggerPayload),
  action = toWorkflowAction(actionType, actionPayload),
  isBuiltIn = isBuiltIn,
  useCount = useCount,
  createdAt = createdAt.toLocalDateTimeUtc(),
  version = version,
  syncState = SyncState.valueOf(syncState)
)
