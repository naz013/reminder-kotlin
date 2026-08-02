package com.github.naz013.tags.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index

/**
 * [itemId] refers to a Reminder or Note uuid owned by the app's main database - Tags has no
 * foreign key to it since the two live in separate Room databases by design (see the Tags
 * feature plan for the isolation trade-off this implies).
 */
@Entity(
  tableName = "TagAssignment",
  primaryKeys = ["tagId", "itemId", "itemType"],
  indices = [
    Index(value = ["itemId", "itemType"]),
    Index(value = ["tagId"])
  ]
)
@Keep
internal data class TagAssignmentEntity(
  val tagId: String,
  val itemId: String,
  val itemType: String
)
