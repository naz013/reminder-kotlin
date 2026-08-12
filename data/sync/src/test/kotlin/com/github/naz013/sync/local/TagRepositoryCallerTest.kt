package com.github.naz013.sync.local

import com.github.naz013.domain.Tag
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.TagRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TagRepositoryCallerTest {
  private val tagRepository = mockk<TagRepository>()
  private val caller = TagRepositoryCaller(tagRepository)

  @Test
  fun `getById delegates to the repository`() = runTest {
    coEvery { tagRepository.getById("1") } returns Tag(id = "1", name = "Work", color = 0)

    val result = caller.getById("1")

    assertEquals("1", result?.id)
  }

  @Test
  fun `getIdsByState delegates to the repository`() = runTest {
    coEvery { tagRepository.getIdsByState(listOf(SyncState.WaitingForUpload)) } returns listOf("1")

    val result = caller.getIdsByState(listOf(SyncState.WaitingForUpload))

    assertEquals(listOf("1"), result)
  }

  @Test
  fun `updateSyncState delegates to the repository`() = runTest {
    coEvery { tagRepository.updateSyncState("1", SyncState.Synced) } returns Unit

    caller.updateSyncState("1", SyncState.Synced)

    coVerify { tagRepository.updateSyncState("1", SyncState.Synced) }
  }

  @Test
  fun `insertOrUpdate saves the tag as Synced`() = runTest {
    coEvery { tagRepository.save(any()) } returns Unit

    caller.insertOrUpdate(Tag(id = "1", name = "Work", color = 0, syncState = SyncState.WaitingForUpload))

    coVerify { tagRepository.save(match { it.id == "1" && it.syncState == SyncState.Synced }) }
  }

  @Test(expected = IllegalArgumentException::class)
  fun `insertOrUpdate rejects an item that is not a Tag`() = runTest {
    caller.insertOrUpdate("not a tag")
  }

  @Test
  fun `getAllIds delegates to the repository`() = runTest {
    coEvery { tagRepository.getAllIds() } returns listOf("1", "2")

    val result = caller.getAllIds()

    assertEquals(listOf("1", "2"), result)
  }
}
