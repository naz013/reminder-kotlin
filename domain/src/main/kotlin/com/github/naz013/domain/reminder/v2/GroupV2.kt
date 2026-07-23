package com.github.naz013.domain.reminder.v2

import com.github.naz013.domain.sync.SyncState
import org.threeten.bp.LocalDateTime
import java.util.UUID

data class GroupV2(
  val uuId: String = UUID.randomUUID().toString(),
  val title: String = "",
  val color: Int = 0,
  val isDefault: Boolean = false,
  val notification: NotificationSettingsOverride = NotificationSettingsOverride(),
  val createdAt: LocalDateTime = LocalDateTime.now(),
  val version: Long = 0L,
  val syncState: SyncState = SyncState.WaitingForUpload
)
