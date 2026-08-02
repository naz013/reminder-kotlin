package com.github.naz013.tags.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "Tag")
@Keep
internal data class TagEntity(
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  val name: String = "",
  val color: Int = 0
)
