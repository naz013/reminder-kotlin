package com.github.naz013.logic.search

enum class SearchResultType {
  REMINDER,
  NOTE,
  BIRTHDAY,
  GOOGLE_TASK,
  GROUP,
  PLACE,
  ROUTINE,
  HABIT
}

data class SearchResult(
  val type: SearchResultType,
  val id: String,
  val title: String,
  val subtitle: String? = null,
  val matchedTag: String? = null
)
