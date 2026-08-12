package com.github.naz013.domain.workflow

import com.github.naz013.domain.sync.SyncState
import org.threeten.bp.LocalDateTime
import java.util.UUID

/** A reusable trigger+action blueprint, unassigned to any specific target. Applying it to a
 * [WorkflowScope] creates a concrete [WorkflowRule] — mirrors how [com.github.naz013.domain.RecurPreset]
 * is a reusable recurrence blueprint applied to create a concrete Reminder's recurrence. */
data class WorkflowTemplate(
  val id: String = UUID.randomUUID().toString(),
  val title: String = "",
  val description: String? = null,
  val category: WorkflowTemplateCategory = WorkflowTemplateCategory.REMINDER_LIFECYCLE,
  val supportedScopeTypes: List<WorkflowScopeType> = WorkflowScopeType.entries,
  val trigger: WorkflowTrigger,
  val action: WorkflowAction,
  val isBuiltIn: Boolean = true,
  val useCount: Int = 0,
  val createdAt: LocalDateTime = LocalDateTime.now(),
  val version: Long = 0L,
  val syncState: SyncState = SyncState.WaitingForUpload
)

/** Which kind(s) of target a [WorkflowTemplate] can be applied to — no specific id, since a
 * template isn't attached to anything until it's applied. */
enum class WorkflowScopeType { GLOBAL, GROUP, REMINDER }

/** Maps 1:1 onto the sections of docs/workflow-engine-research.md's workflow catalog. */
enum class WorkflowTemplateCategory {
  REMINDER_LIFECYCLE,
  GROUP,
  LOCATION,
  NOTIFICATION_ESCALATION,
  SYSTEM_INTEGRATION,
  PRIVACY_DATA_HYGIENE
}

fun WorkflowScope.type(): WorkflowScopeType = when (this) {
  is WorkflowScope.Global -> WorkflowScopeType.GLOBAL
  is WorkflowScope.ForGroup -> WorkflowScopeType.GROUP
  is WorkflowScope.ForReminder -> WorkflowScopeType.REMINDER
}
