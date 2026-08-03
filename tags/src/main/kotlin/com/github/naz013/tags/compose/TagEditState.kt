package com.github.naz013.tags.compose

data class TagEditState(
  val id: String? = null,
  val name: String = "",
  val nameError: Boolean = false,
  val colorPosition: Int = 0,
  val canDelete: Boolean = false
)
