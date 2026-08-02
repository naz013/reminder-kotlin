package com.github.naz013.tags.impl

import com.github.naz013.logging.Logger
import com.github.naz013.tags.Tag
import com.github.naz013.tags.TagAssignmentRepository
import com.github.naz013.tags.TaggedItemType
import com.github.naz013.tags.db.dao.TagAssignmentDao
import com.github.naz013.tags.db.entity.TagAssignmentEntity
import com.github.naz013.tags.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class TagAssignmentRepositoryImpl(
  private val dao: TagAssignmentDao
) : TagAssignmentRepository {

  override fun observeTagsForItem(itemId: String, itemType: TaggedItemType): Flow<List<Tag>> =
    dao.observeTagsForItem(itemId, itemType.name).map { list -> list.map { it.toDomain() } }

  override suspend fun getTagsForItem(itemId: String, itemType: TaggedItemType): List<Tag> =
    dao.getTagsForItem(itemId, itemType.name).map { it.toDomain() }

  override suspend fun getItemIdsForTag(tagId: String, itemType: TaggedItemType): List<String> =
    dao.getItemIdsForTag(tagId, itemType.name)

  override suspend fun attach(itemId: String, itemType: TaggedItemType, tagId: String) {
    Logger.d(TAG, "Attach tag $tagId to $itemType:$itemId")
    dao.insert(TagAssignmentEntity(tagId = tagId, itemId = itemId, itemType = itemType.name))
  }

  override suspend fun detach(itemId: String, itemType: TaggedItemType, tagId: String) {
    Logger.d(TAG, "Detach tag $tagId from $itemType:$itemId")
    dao.delete(tagId = tagId, itemId = itemId, itemType = itemType.name)
  }

  override suspend fun detachAll(itemId: String, itemType: TaggedItemType) {
    Logger.d(TAG, "Detach all tags from $itemType:$itemId")
    dao.deleteAllForItem(itemId, itemType.name)
  }

  companion object {
    private const val TAG = "TagAssignmentRepository"
  }
}
