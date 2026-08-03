package com.github.naz013.repository.impl

import com.github.naz013.domain.TagAssignment
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.repository.dao.TagAssignmentDao
import com.github.naz013.repository.entity.TagAssignmentEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TagAssignmentRepositoryImplTest {
  private val dao = mockk<TagAssignmentDao>(relaxed = true)
  private val notifier = mockk<TableChangeNotifier>(relaxed = true)
  private lateinit var repository: TagAssignmentRepositoryImpl

  @Before
  fun setUp() {
    repository = TagAssignmentRepositoryImpl(dao, notifier)
  }

  @Test
  fun `attach inserts an assignment row and notifies`() = runTest {
    repository.attach(itemId = "note-1", itemType = TaggedItemType.NOTE, tagId = "tag-1")

    verify {
      dao.insert(TagAssignmentEntity(tagId = "tag-1", itemId = "note-1", itemType = "NOTE"))
    }
  }

  @Test
  fun `detach removes the matching assignment row`() = runTest {
    repository.detach(itemId = "note-1", itemType = TaggedItemType.NOTE, tagId = "tag-1")

    verify { dao.delete(tagId = "tag-1", itemId = "note-1", itemType = "NOTE") }
  }

  @Test
  fun `getAll maps every assignment row to a domain TagAssignment`() = runTest {
    every { dao.getAll() } returns listOf(
      TagAssignmentEntity(tagId = "tag-1", itemId = "note-1", itemType = "NOTE")
    )

    val result = repository.getAll()

    assertEquals(
      listOf(TagAssignment(tagId = "tag-1", itemId = "note-1", itemType = TaggedItemType.NOTE)),
      result
    )
  }

  @Test
  fun `replaceAll performs an atomic full replace via the dao`() = runTest {
    val assignments = listOf(TagAssignment(tagId = "tag-1", itemId = "note-1", itemType = TaggedItemType.NOTE))

    repository.replaceAll(assignments)

    verify {
      dao.replaceAll(listOf(TagAssignmentEntity(tagId = "tag-1", itemId = "note-1", itemType = "NOTE")))
    }
  }
}
