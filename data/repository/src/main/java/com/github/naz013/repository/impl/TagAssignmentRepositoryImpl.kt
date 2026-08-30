package com.github.naz013.repository.impl

import com.github.naz013.domain.Tag
import com.github.naz013.domain.TagAssignment
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.logging.Logger
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.dao.TagAssignmentDao
import com.github.naz013.repository.entity.TagAssignmentEntity
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class TagAssignmentRepositoryImpl(
  private val dao: TagAssignmentDao,
  private val tableChangeNotifier: TableChangeNotifier
) : TagAssignmentRepository {

  private val table = Table.TagAssignment

  override fun observeTagsForItem(itemId: String, itemType: TaggedItemType): Flow<List<Tag>> =
    dao.observeTagsForItem(itemId, itemType.name).map { list -> list.map { it.toDomain() } }

  override suspend fun getTagsForItem(itemId: String, itemType: TaggedItemType): List<Tag> =
    dao.getTagsForItem(itemId, itemType.name).map { it.toDomain() }

  override suspend fun getItemIdsForTag(tagId: String, itemType: TaggedItemType): List<String> =
    dao.getItemIdsForTag(tagId, itemType.name)

  override fun observeItemIdsForTag(tagId: String, itemType: TaggedItemType): Flow<List<String>> =
    dao.observeItemIdsForTag(tagId, itemType.name)

  override suspend fun attach(itemId: String, itemType: TaggedItemType, tagId: String) {
    Logger.d(TAG, "Attach tag $tagId to $itemType:$itemId")
    dao.insert(TagAssignmentEntity(tagId = tagId, itemId = itemId, itemType = itemType.name))
    tableChangeNotifier.notify(table)
  }

  override suspend fun detach(itemId: String, itemType: TaggedItemType, tagId: String) {
    Logger.d(TAG, "Detach tag $tagId from $itemType:$itemId")
    dao.delete(tagId = tagId, itemId = itemId, itemType = itemType.name)
    tableChangeNotifier.notify(table)
  }

  override suspend fun detachAll(itemId: String, itemType: TaggedItemType) {
    Logger.d(TAG, "Detach all tags from $itemType:$itemId")
    dao.deleteAllForItem(itemId, itemType.name)
    tableChangeNotifier.notify(table)
  }

  override suspend fun detachAllForTag(tagId: String) {
    Logger.d(TAG, "Detach tag $tagId from every item")
    dao.deleteAllForTag(tagId)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all tag assignments")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  override suspend fun getAll(): List<TagAssignment> {
    Logger.d(TAG, "Get all tag assignments")
    return dao.getAll().map { it.toDomain() }
  }

  override suspend fun replaceAll(assignments: List<TagAssignment>) {
    Logger.d(TAG, "Replace all tag assignments, size = ${assignments.size}")
    dao.replaceAll(assignments.map { it.toEntity() })
    tableChangeNotifier.notify(table)
  }

  companion object {
    private const val TAG = "TagAssignmentRepository"
  }
}
