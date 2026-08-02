package com.github.naz013.tags

import com.github.naz013.tags.db.entity.TagEntity

internal fun Tag.toEntity(): TagEntity = TagEntity(id = id, name = name, color = color)

internal fun TagEntity.toDomain(): Tag = Tag(id = id, name = name, color = color)
