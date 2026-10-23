package com.github.naz013.feature.common.viewmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PendingDeleteCoordinatorTest {

  private fun TestScope.coordinator(): PendingDeleteCoordinator {
    val dispatcher = StandardTestDispatcher(testScheduler)
    return PendingDeleteCoordinatorImpl(this, dispatcher)
  }

  @Test
  fun `markPending hides the id in pendingIds`() = runTest {
    val coordinator = coordinator()

    coordinator.markPending(PendingEntity.NOTE, "1", "Deleted") { }

    assertEquals(setOf("1"), coordinator.pendingIds(PendingEntity.NOTE).value)
  }

  @Test
  fun `undo restores the id and never runs onCommit`() = runTest {
    val coordinator = coordinator()
    var commits = 0
    coordinator.markPending(PendingEntity.NOTE, "1", "Deleted") { commits++ }

    coordinator.controller(PendingEntity.NOTE).undo("1")
    advanceTimeBy(UNDO_WINDOW_MS + UNDO_WATCHDOG_GRACE_MS + 1)
    runCurrent()

    assertEquals(emptySet<String>(), coordinator.pendingIds(PendingEntity.NOTE).value)
    assertEquals(0, commits)
  }

  @Test
  fun `host timeout commits exactly once`() = runTest {
    val coordinator = coordinator()
    var commits = 0
    coordinator.markPending(PendingEntity.NOTE, "1", "Deleted") { commits++ }

    val controller = coordinator.controller(PendingEntity.NOTE)
    controller.commit("1")
    controller.commit("1")
    advanceUntilIdle()

    assertEquals(1, commits)
    assertEquals(emptySet<String>(), coordinator.pendingIds(PendingEntity.NOTE).value)
  }

  @Test
  fun `watchdog commits when nobody resolves the delete`() = runTest {
    val coordinator = coordinator()
    var commits = 0
    coordinator.markPending(PendingEntity.NOTE, "1", "Deleted") { commits++ }

    advanceTimeBy(UNDO_WINDOW_MS)
    runCurrent()
    assertEquals(0, commits)
    advanceTimeBy(UNDO_WATCHDOG_GRACE_MS + 1)
    runCurrent()

    assertEquals(1, commits)
  }

  @Test
  fun `entity types are independent`() = runTest {
    val coordinator = coordinator()
    var noteCommits = 0
    var tagCommits = 0
    coordinator.markPending(PendingEntity.NOTE, "1", "m") { noteCommits++ }
    coordinator.markPending(PendingEntity.TAG, "2", "m") { tagCommits++ }

    coordinator.controller(PendingEntity.NOTE).commit("1")
    advanceTimeBy(1)
    runCurrent()

    assertEquals(1, noteCommits)
    assertEquals(0, tagCommits)
    assertEquals(setOf("2"), coordinator.pendingIds(PendingEntity.TAG).value)
  }

  @Test
  fun `second mark of the same type commits the first`() = runTest {
    val coordinator = coordinator()
    var firstCommits = 0
    var secondCommits = 0
    coordinator.markPending(PendingEntity.NOTE, "1", "m") { firstCommits++ }

    coordinator.markPending(PendingEntity.NOTE, "2", "m") { secondCommits++ }
    runCurrent()

    assertEquals(1, firstCommits)
    assertEquals(0, secondCommits)
    assertEquals(setOf("2"), coordinator.pendingIds(PendingEntity.NOTE).value)
  }

  @Test
  fun `commitAll flushes every type`() = runTest {
    val coordinator = coordinator()
    var commits = 0
    coordinator.markPending(PendingEntity.NOTE, "1", "m") { commits++ }
    coordinator.markPending(PendingEntity.REMINDER, "2", "m") { commits++ }

    coordinator.commitAll()
    advanceTimeBy(1)
    runCurrent()

    assertEquals(2, commits)
    assertEquals(emptySet<String>(), coordinator.pendingIds(PendingEntity.NOTE).value)
    assertEquals(emptySet<String>(), coordinator.pendingIds(PendingEntity.REMINDER).value)
  }

  @Test
  fun `host count follows register and unregister`() = runTest {
    val coordinator = coordinator()

    coordinator.registerHost(PendingEntity.NOTE)
    coordinator.registerHost(PendingEntity.NOTE)
    coordinator.unregisterHost(PendingEntity.NOTE)

    assertEquals(1, coordinator.hostCount(PendingEntity.NOTE).value)
    assertEquals(0, coordinator.hostCount(PendingEntity.TAG).value)
  }

  @Test
  fun `host count stays at zero when unregistered without a register`() = runTest {
    val coordinator = coordinator()

    coordinator.unregisterHost(PendingEntity.NOTE)
    assertEquals(0, coordinator.hostCount(PendingEntity.NOTE).value)

    coordinator.registerHost(PendingEntity.NOTE)
    assertEquals(1, coordinator.hostCount(PendingEntity.NOTE).value)
  }

  @Test
  fun `watchdog does not commit again after the host already committed`() = runTest {
    val coordinator = coordinator()
    var commits = 0
    coordinator.markPending(PendingEntity.NOTE, "1", "Deleted") { commits++ }
    coordinator.controller(PendingEntity.NOTE).commit("1")
    runCurrent()

    advanceTimeBy(UNDO_WINDOW_MS + UNDO_WATCHDOG_GRACE_MS + 1)
    runCurrent()

    assertEquals(1, commits)
  }

  @Test
  fun `watchdog of an older mark does not commit a newer mark of the same id`() = runTest {
    val coordinator = coordinator()
    var olderCommits = 0
    var newerCommits = 0
    coordinator.markPending(PendingEntity.NOTE, "1", "m") { olderCommits++ }
    advanceTimeBy(2_000)
    runCurrent()

    coordinator.markPending(PendingEntity.NOTE, "1", "m") { newerCommits++ }
    runCurrent()
    assertEquals(1, olderCommits)

    advanceTimeBy(UNDO_WINDOW_MS + UNDO_WATCHDOG_GRACE_MS - 2_000 + 1)
    runCurrent()
    assertEquals(0, newerCommits)
    assertEquals(setOf("1"), coordinator.pendingIds(PendingEntity.NOTE).value)

    advanceTimeBy(2_000)
    runCurrent()
    assertEquals(1, newerCommits)
    assertEquals(1, olderCommits)
    assertEquals(emptySet<String>(), coordinator.pendingIds(PendingEntity.NOTE).value)
  }

  @Test
  fun `commitAll with nothing pending leaves every type empty`() = runTest {
    val coordinator = coordinator()

    coordinator.commitAll()
    advanceUntilIdle()

    PendingEntity.entries.forEach {
      assertEquals(emptySet<String>(), coordinator.pendingIds(it).value)
    }
  }

  @Test
  fun `undo cancels the watchdog`() = runTest {
    val coordinator = coordinator()
    coordinator.markPending(PendingEntity.NOTE, "1", "m") { }
    runCurrent()
    assertEquals(1, coroutineContext[Job]!!.children.count())

    coordinator.controller(PendingEntity.NOTE).undo("1")
    runCurrent()

    assertEquals(0, coroutineContext[Job]!!.children.count())
  }

  @Test
  fun `latest event clears when the delete is resolved elsewhere`() = runTest {
    val coordinator = coordinator()
    val controller = coordinator.controller(PendingEntity.NOTE)
    coordinator.markPending(PendingEntity.NOTE, "1", "m") { }
    assertEquals("1", controller.latestEvent.value?.batchKey)

    coordinator.commitAll()

    assertEquals(null, controller.latestEvent.value)
  }
}
