package com.github.naz013.tags.impl

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.github.naz013.tags.Tag
import com.github.naz013.tags.TaggedItemType
import com.github.naz013.tags.db.TagsDb
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TagRepositoryImplTest {

  private lateinit var db: TagsDb
  private lateinit var tagRepository: TagRepositoryImpl
  private lateinit var tagAssignmentRepository: TagAssignmentRepositoryImpl

  @Before
  fun setUp() {
    db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), TagsDb::class.java)
      .allowMainThreadQueries()
      .build()
    tagRepository = TagRepositoryImpl(db.tagDao())
    tagAssignmentRepository = TagAssignmentRepositoryImpl(db.tagAssignmentDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `save then getById returns the saved tag`() = runTest {
    val tag = Tag(id = "tag-1", name = "Work", color = 0xFF0000)

    tagRepository.save(tag)
    val result = tagRepository.getById("tag-1")

    assertEquals(tag, result)
  }

  @Test
  fun `getById returns null for an unknown id`() = runTest {
    val result = tagRepository.getById("missing")

    assertNull(result)
  }

  @Test
  fun `delete removes the tag`() = runTest {
    tagRepository.save(Tag(id = "tag-1", name = "Work", color = 0))

    tagRepository.delete("tag-1")

    assertNull(tagRepository.getById("tag-1"))
  }

  @Test
  fun `getAll returns tags ordered by name`() = runTest {
    tagRepository.save(Tag(id = "tag-b", name = "Zebra", color = 0))
    tagRepository.save(Tag(id = "tag-a", name = "Apple", color = 0))

    val result = tagRepository.getAll()

    assertEquals(listOf("Apple", "Zebra"), result.map { it.name })
  }

  @Test
  fun `attach then getTagsForItem returns the attached tag`() = runTest {
    val tag = Tag(id = "tag-1", name = "Work", color = 0)
    tagRepository.save(tag)

    tagAssignmentRepository.attach(itemId = "reminder-1", itemType = TaggedItemType.REMINDER, tagId = "tag-1")
    val result = tagAssignmentRepository.getTagsForItem("reminder-1", TaggedItemType.REMINDER)

    assertEquals(listOf(tag), result)
  }

  @Test
  fun `attach is scoped by item type - a note with the same id is not tagged`() = runTest {
    tagRepository.save(Tag(id = "tag-1", name = "Work", color = 0))

    tagAssignmentRepository.attach(itemId = "same-id", itemType = TaggedItemType.REMINDER, tagId = "tag-1")
    val noteTags = tagAssignmentRepository.getTagsForItem("same-id", TaggedItemType.NOTE)

    assertTrue(noteTags.isEmpty())
  }

  @Test
  fun `detach removes only the given tag from the item`() = runTest {
    tagRepository.save(Tag(id = "tag-1", name = "Work", color = 0))
    tagRepository.save(Tag(id = "tag-2", name = "Home", color = 0))
    tagAssignmentRepository.attach("reminder-1", TaggedItemType.REMINDER, "tag-1")
    tagAssignmentRepository.attach("reminder-1", TaggedItemType.REMINDER, "tag-2")

    tagAssignmentRepository.detach("reminder-1", TaggedItemType.REMINDER, "tag-1")
    val result = tagAssignmentRepository.getTagsForItem("reminder-1", TaggedItemType.REMINDER)

    assertEquals(listOf("Home"), result.map { it.name })
  }

  @Test
  fun `detachAll removes every tag from the item`() = runTest {
    tagRepository.save(Tag(id = "tag-1", name = "Work", color = 0))
    tagRepository.save(Tag(id = "tag-2", name = "Home", color = 0))
    tagAssignmentRepository.attach("reminder-1", TaggedItemType.REMINDER, "tag-1")
    tagAssignmentRepository.attach("reminder-1", TaggedItemType.REMINDER, "tag-2")

    tagAssignmentRepository.detachAll("reminder-1", TaggedItemType.REMINDER)
    val result = tagAssignmentRepository.getTagsForItem("reminder-1", TaggedItemType.REMINDER)

    assertTrue(result.isEmpty())
  }

  @Test
  fun `getItemIdsForTag returns every item id tagged with that tag`() = runTest {
    tagRepository.save(Tag(id = "tag-1", name = "Work", color = 0))
    tagAssignmentRepository.attach("reminder-1", TaggedItemType.REMINDER, "tag-1")
    tagAssignmentRepository.attach("reminder-2", TaggedItemType.REMINDER, "tag-1")

    val result = tagAssignmentRepository.getItemIdsForTag("tag-1", TaggedItemType.REMINDER)

    assertEquals(setOf("reminder-1", "reminder-2"), result.toSet())
  }

  @Test
  fun `detachAllForTag removes the tag from every item it was attached to`() = runTest {
    tagRepository.save(Tag(id = "tag-1", name = "Work", color = 0))
    tagAssignmentRepository.attach("reminder-1", TaggedItemType.REMINDER, "tag-1")
    tagAssignmentRepository.attach("note-1", TaggedItemType.NOTE, "tag-1")

    tagAssignmentRepository.detachAllForTag("tag-1")

    assertTrue(tagAssignmentRepository.getTagsForItem("reminder-1", TaggedItemType.REMINDER).isEmpty())
    assertTrue(tagAssignmentRepository.getTagsForItem("note-1", TaggedItemType.NOTE).isEmpty())
  }

  @Test
  fun `attaching the same tag to the same item twice does not fail or duplicate`() = runTest {
    tagRepository.save(Tag(id = "tag-1", name = "Work", color = 0))

    tagAssignmentRepository.attach("reminder-1", TaggedItemType.REMINDER, "tag-1")
    tagAssignmentRepository.attach("reminder-1", TaggedItemType.REMINDER, "tag-1")
    val result = tagAssignmentRepository.getTagsForItem("reminder-1", TaggedItemType.REMINDER)

    assertEquals(1, result.size)
  }
}
