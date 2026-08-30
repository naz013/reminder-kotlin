package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.naz013.repository.entity.TagAssignmentEntity
import com.github.naz013.repository.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TagAssignmentDao {

  @Query(
    """
    SELECT Tag.* FROM Tag
    INNER JOIN TagAssignment ON Tag.id = TagAssignment.tagId
    WHERE TagAssignment.itemId = :itemId AND TagAssignment.itemType = :itemType
    ORDER BY Tag.name
    """
  )
  fun observeTagsForItem(itemId: String, itemType: String): Flow<List<TagEntity>>

  @Query(
    """
    SELECT Tag.* FROM Tag
    INNER JOIN TagAssignment ON Tag.id = TagAssignment.tagId
    WHERE TagAssignment.itemId = :itemId AND TagAssignment.itemType = :itemType
    ORDER BY Tag.name
    """
  )
  fun getTagsForItem(itemId: String, itemType: String): List<TagEntity>

  @Query("SELECT itemId FROM TagAssignment WHERE tagId=:tagId AND itemType=:itemType")
  fun getItemIdsForTag(tagId: String, itemType: String): List<String>

  @Query("SELECT itemId FROM TagAssignment WHERE tagId=:tagId AND itemType=:itemType")
  fun observeItemIdsForTag(tagId: String, itemType: String): Flow<List<String>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insert(assignment: TagAssignmentEntity)

  @Query("DELETE FROM TagAssignment WHERE tagId=:tagId AND itemId=:itemId AND itemType=:itemType")
  fun delete(tagId: String, itemId: String, itemType: String)

  @Query("DELETE FROM TagAssignment WHERE itemId=:itemId AND itemType=:itemType")
  fun deleteAllForItem(itemId: String, itemType: String)

  @Query("DELETE FROM TagAssignment WHERE tagId=:tagId")
  fun deleteAllForTag(tagId: String)

  @Query("SELECT * FROM TagAssignment")
  fun getAll(): List<TagAssignmentEntity>

  @Query("DELETE FROM TagAssignment")
  fun deleteAll()

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertAll(assignments: List<TagAssignmentEntity>)

  @Transaction
  fun replaceAll(assignments: List<TagAssignmentEntity>) {
    deleteAll()
    insertAll(assignments)
  }
}
