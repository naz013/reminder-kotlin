package com.github.naz013.feature.common.viewmodel

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingDeleteTrackerTest {

  @Test
  fun `markPending hides the given ids immediately`() {
    val tracker = PendingDeleteTracker()

    tracker.markPending(batchKey = "1", ids = setOf("1")) { }

    assertEquals(setOf("1"), tracker.pendingIds.value)
  }

  @Test
  fun `markPending keeps ids from different batches independent`() {
    val tracker = PendingDeleteTracker()

    tracker.markPending(batchKey = "a", ids = setOf("1")) { }
    tracker.markPending(batchKey = "b", ids = setOf("2", "3")) { }

    assertEquals(setOf("1", "2", "3"), tracker.pendingIds.value)
  }

  @Test
  fun `undo un-hides the ids and never runs the commit action`() =
    runBlocking {
      val tracker = PendingDeleteTracker()
      var committed = false
      tracker.markPending(batchKey = "1", ids = setOf("1")) { committed = true }

      tracker.undo("1")

      assertEquals(emptySet<String>(), tracker.pendingIds.value)
      assertTrue(!committed)
    }

  @Test
  fun `undo only affects its own batch`() {
    val tracker = PendingDeleteTracker()
    tracker.markPending(batchKey = "a", ids = setOf("1")) { }
    tracker.markPending(batchKey = "b", ids = setOf("2")) { }

    tracker.undo("a")

    assertEquals(setOf("2"), tracker.pendingIds.value)
  }

  @Test
  fun `undo on an unknown batch key does nothing`() {
    val tracker = PendingDeleteTracker()
    tracker.markPending(batchKey = "1", ids = setOf("1")) { }

    tracker.undo("unknown")

    assertEquals(setOf("1"), tracker.pendingIds.value)
  }

  @Test
  fun `commit runs the commit action and un-hides the ids`() =
    runBlocking {
      val tracker = PendingDeleteTracker()
      var committed = false
      tracker.markPending(batchKey = "1", ids = setOf("1")) { committed = true }

      tracker.commit("1")

      assertTrue(committed)
      assertEquals(emptySet<String>(), tracker.pendingIds.value)
    }

  @Test
  fun `commit on an unknown batch key does nothing`() =
    runBlocking {
      val tracker = PendingDeleteTracker()

      tracker.commit("unknown")
    }

  @Test
  fun `commit after undo does not run the commit action again`() =
    runBlocking {
      val tracker = PendingDeleteTracker()
      var committed = false
      tracker.markPending(batchKey = "1", ids = setOf("1")) { committed = true }
      tracker.undo("1")

      tracker.commit("1")

      assertTrue(!committed)
    }

  @Test
  fun `re-marking the same batch key replaces its ids and commit action`() =
    runBlocking {
      val tracker = PendingDeleteTracker()
      var firstCommitted = false
      var secondCommitted = false
      tracker.markPending(batchKey = "1", ids = setOf("1")) { firstCommitted = true }

      tracker.markPending(batchKey = "1", ids = setOf("2")) { secondCommitted = true }
      tracker.commit("1")

      assertTrue(!firstCommitted)
      assertTrue(secondCommitted)
      assertEquals(emptySet<String>(), tracker.pendingIds.value)
    }
}
