package com.github.naz013.tags

import kotlinx.coroutines.flow.Flow

interface TagRepository {
  fun observeAll(): Flow<List<Tag>>

  suspend fun getAll(): List<Tag>

  suspend fun getById(id: String): Tag?

  suspend fun save(tag: Tag)

  suspend fun delete(id: String)
}
