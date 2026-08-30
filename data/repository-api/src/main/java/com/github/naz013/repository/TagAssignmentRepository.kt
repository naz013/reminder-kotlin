package com.github.naz013.repository

import com.github.naz013.domain.Tag
import com.github.naz013.domain.TagAssignment
import com.github.naz013.domain.TaggedItemType
import kotlinx.coroutines.flow.Flow

interface TagAssignmentRepository {
  fun observeTagsForItem(itemId: String, itemType: TaggedItemType): Flow<List<Tag>>

  suspend fun getTagsForItem(itemId: String, itemType: TaggedItemType): List<Tag>

  suspend fun getItemIdsForTag(tagId: String, itemType: TaggedItemType): List<String>

  fun observeItemIdsForTag(tagId: String, itemType: TaggedItemType): Flow<List<String>>

  suspend fun attach(itemId: String, itemType: TaggedItemType, tagId: String)

  suspend fun detach(itemId: String, itemType: TaggedItemType, tagId: String)

  suspend fun detachAll(itemId: String, itemType: TaggedItemType)

  suspend fun detachAllForTag(tagId: String)

  suspend fun deleteAll()

  suspend fun getAll(): List<TagAssignment>

  suspend fun replaceAll(assignments: List<TagAssignment>)
}
