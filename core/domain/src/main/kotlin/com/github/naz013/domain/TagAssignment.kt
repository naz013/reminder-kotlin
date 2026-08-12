package com.github.naz013.domain

data class TagAssignment(
  val tagId: String,
  val itemId: String,
  val itemType: TaggedItemType
)
