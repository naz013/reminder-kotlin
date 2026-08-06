package com.github.naz013.repository.entity

import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState

internal fun GroupV2.toEntity(): GroupV2Entity = GroupV2Entity(
  uuId = uuId,
  title = title,
  color = color,
  isDefault = isDefault,
  notification = notification.toColumns(),
  createdAt = createdAt.toEpochMillisUtc(),
  version = version,
  syncState = syncState.name
)

internal fun GroupV2Entity.toDomain(): GroupV2 = GroupV2(
  uuId = uuId,
  title = title,
  color = color,
  isDefault = isDefault,
  notification = notification.toDomain(),
  createdAt = createdAt.toLocalDateTimeUtc(),
  version = version,
  syncState = SyncState.valueOf(syncState)
)
