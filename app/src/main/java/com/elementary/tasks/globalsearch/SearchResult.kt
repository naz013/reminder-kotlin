package com.elementary.tasks.globalsearch

sealed class SearchResult(
  open val query: String
)

data class RecentSearchResult(
  val text: String,
  val id: Long,
  override val query: String
) : SearchResult(query)

data class RecentObjectSearchResult(
  val text: String,
  val id: Long,
  val objectType: ObjectType,
  val objectId: String,
  override val query: String
) : SearchResult(query)

data class ObjectSearchResult(
  val text: String,
  val objectType: ObjectType,
  val objectId: String,
  override val query: String
) : SearchResult(query)

enum class ObjectType {
  REMINDER, NOTE, BIRTHDAY, PLACE, GROUP, GOOGLE_TASK
}
