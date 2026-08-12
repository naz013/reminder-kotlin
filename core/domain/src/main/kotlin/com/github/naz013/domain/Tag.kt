package com.github.naz013.domain

import com.github.naz013.domain.sync.SyncState
import java.util.UUID

data class Tag(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val color: Int,
  val version: Long = 0L,
  val syncState: SyncState = SyncState.WaitingForUpload
)
