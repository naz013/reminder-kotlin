package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
  tableName = "Tag",
  indices = [
    Index(value = ["syncState"])
  ]
)
@Keep
internal data class TagEntity(
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  val name: String = "",
  val color: Int = 0,
  val version: Long = 0L,
  val syncState: String
)
