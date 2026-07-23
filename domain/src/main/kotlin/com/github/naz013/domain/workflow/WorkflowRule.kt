package com.github.naz013.domain.workflow

import com.github.naz013.domain.sync.SyncState
import org.threeten.bp.LocalDateTime
import java.util.UUID

data class WorkflowRule(
  val uuId: String = UUID.randomUUID().toString(),
  val title: String = "",
  val scope: WorkflowScope = WorkflowScope.Global,
  val trigger: WorkflowTrigger,
  val action: WorkflowAction,
  val isEnabled: Boolean = true,
  val createdAt: LocalDateTime = LocalDateTime.now(),
  val lastRunAt: LocalDateTime? = null,
  val version: Long = 0L,
  val syncState: SyncState = SyncState.WaitingForUpload
)

sealed class WorkflowScope {
  data object Global : WorkflowScope()

  data class ForGroup(
    val groupId: String
  ) : WorkflowScope()

  data class ForReminder(
    val reminderId: String
  ) : WorkflowScope()
}
