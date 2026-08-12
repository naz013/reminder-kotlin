package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index

/**
 * [itemId] refers to a Reminder or Note id - no foreign key, since orphaned assignment rows
 * (an item was deleted without detaching its tags first) are harmless: they simply never match a
 * real item again.
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
