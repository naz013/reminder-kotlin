package com.github.naz013.tags

import kotlinx.coroutines.flow.Flow

interface TagAssignmentRepository {
  fun observeTagsForItem(itemId: String, itemType: TaggedItemType): Flow<List<Tag>>

  suspend fun getTagsForItem(itemId: String, itemType: TaggedItemType): List<Tag>

  suspend fun getItemIdsForTag(tagId: String, itemType: TaggedItemType): List<String>

  suspend fun attach(itemId: String, itemType: TaggedItemType, tagId: String)

  suspend fun detach(itemId: String, itemType: TaggedItemType, tagId: String)

  suspend fun detachAll(itemId: String, itemType: TaggedItemType)

  suspend fun detachAllForTag(tagId: String)
}
