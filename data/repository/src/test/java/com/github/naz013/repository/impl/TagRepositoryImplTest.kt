package com.github.naz013.repository.impl

import com.github.naz013.domain.Tag
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.TagSyncTrigger
import com.github.naz013.repository.dao.TagDao
import com.github.naz013.repository.entity.TagEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TagRepositoryImplTest {
  private val dao = mockk<TagDao>(relaxed = true)
  private val notifier = mockk<TableChangeNotifier>(relaxed = true)
  private val tagSyncTrigger = mockk<TagSyncTrigger>(relaxed = true)
  private lateinit var repository: TagRepositoryImpl

  @Before
  fun setUp() {
    repository = TagRepositoryImpl(dao, notifier, tagSyncTrigger)
  }

  private fun tagEntity(id: String, name: String = "name") = TagEntity(
    id = id,
    name = name,
    color = 0,
    version = 0L,
    syncState = SyncState.WaitingForUpload.name
  )

  @Test
  fun `observeAll maps entities to domain tags`() = runTest {
    every { dao.observeAll() } returns flowOf(listOf(tagEntity("1")))

    val result = repository.observeAll().first()

    assertEquals(listOf("1"), result.map { it.id })
  }

  @Test
  fun `save inserts the entity and notifies the sync trigger`() = runTest {
    repository.save(Tag(id = "1", name = "Work", color = 2))

    verify { dao.insert(match { it.id == "1" && it.name == "Work" }) }
    verify { tagSyncTrigger.onTagSaved("1") }
  }

  @Test
  fun `delete removes the entity and notifies the sync trigger`() = runTest {
    repository.delete("1")

    verify { dao.delete("1") }
    verify { tagSyncTrigger.onTagDeleted("1") }
  }

  @Test
  fun `getIdsByState delegates to the dao with sync state names`() = runTest {
    every { dao.getBySyncStates(listOf("WaitingForUpload")) } returns listOf("1")

    val result = repository.getIdsByState(listOf(SyncState.WaitingForUpload))

    assertEquals(listOf("1"), result)
  }

  @Test
  fun `updateSyncState delegates to the dao`() = runTest {
    repository.updateSyncState("1", SyncState.Synced)

    verify { dao.updateSyncState("1", "Synced") }
  }

  @Test
  fun `getAllIds delegates to the dao`() = runTest {
    every { dao.getAllIds() } returns listOf("1", "2")

    val result = repository.getAllIds()

    assertEquals(listOf("1", "2"), result)
  }
}
