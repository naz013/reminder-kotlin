package com.github.naz013.repository.entity

import com.github.naz013.domain.Tag
import com.github.naz013.domain.TagAssignment
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.sync.SyncState

internal fun Tag.toEntity(): TagEntity = TagEntity(
  id = id,
  name = name,
  color = color,
  version = version,
  syncState = syncState.name
)

internal fun TagEntity.toDomain(): Tag = Tag(
  id = id,
  name = name,
  color = color,
  version = version,
  syncState = SyncState.valueOf(syncState)
)

internal fun TagAssignment.toEntity(): TagAssignmentEntity = TagAssignmentEntity(
  tagId = tagId,
  itemId = itemId,
  itemType = itemType.name
)

internal fun TagAssignmentEntity.toDomain(): TagAssignment = TagAssignment(
  tagId = tagId,
  itemId = itemId,
  itemType = TaggedItemType.valueOf(itemType)
)
