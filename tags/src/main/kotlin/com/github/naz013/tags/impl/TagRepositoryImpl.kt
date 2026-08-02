package com.github.naz013.tags.impl

import com.github.naz013.logging.Logger
import com.github.naz013.tags.Tag
import com.github.naz013.tags.TagRepository
import com.github.naz013.tags.db.dao.TagDao
import com.github.naz013.tags.toDomain
import com.github.naz013.tags.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class TagRepositoryImpl(
  private val dao: TagDao
) : TagRepository {

  override fun observeAll(): Flow<List<Tag>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

  override suspend fun getAll(): List<Tag> = dao.getAll().map { it.toDomain() }

  override suspend fun getById(id: String): Tag? = dao.getById(id)?.toDomain()

  override suspend fun save(tag: Tag) {
    Logger.d(TAG, "Save tag: ${tag.id}")
    dao.insert(tag.toEntity())
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete tag: $id")
    dao.delete(id)
  }

  companion object {
    private const val TAG = "TagRepository"
  }
}
