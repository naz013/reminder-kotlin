package com.github.naz013.tags

import java.util.UUID

data class Tag(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val color: Int
)
