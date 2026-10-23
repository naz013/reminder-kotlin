package com.github.naz013.feature.common.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test

class UndoDeleteControllerTest {

  private fun controller(scope: CoroutineScope = CoroutineScope(Job())) =
    UndoDeleteController(scope, Dispatchers.Unconfined)

  private suspend fun UndoDeleteController.currentEvent() =
    withTimeout(1000) { undoEvents.first() }

  @Test
  fun `markPending hides the ids and emits one undo event`() =
    runBlocking {
      val controller = controller()

      controller.markPending("k", setOf("1", "2"), "Deleted") { }

      assertEquals(setOf("1", "2"), controller.pendingIds.value)
      val event = controller.currentEvent()
      assertEquals("k", event.batchKey)
      assertEquals("Deleted", event.message)
    }

  @Test
  fun `undo restores the ids and never commits`() {
    val controller = controller()
    var commits = 0
    controller.markPending("k", setOf("1"), "m") { commits++ }

    controller.undo("k")
    controller.commit("k")

    assertEquals(emptySet<String>(), controller.pendingIds.value)
    assertEquals(0, commits)
  }

  @Test
  fun `commit runs onCommit once`() {
    val controller = controller()
    var commits = 0
    controller.markPending("k", setOf("1"), "m") { commits++ }

    controller.commit("k")
    controller.commit("k")

    assertEquals(1, commits)
    assertEquals(emptySet<String>(), controller.pendingIds.value)
  }

  @Test
  fun `batches are isolated from each other`() {
    val controller = controller()
    var committedA = 0
    var committedB = 0
    controller.markPending("a", setOf("1"), "m") { committedA++ }
    controller.undo("a")
    controller.markPending("b", setOf("2"), "m") { committedB++ }
    controller.commit("b")

    assertEquals(0, committedA)
    assertEquals(1, committedB)
    assertEquals(emptySet<String>(), controller.pendingIds.value)
  }

  @Test
  fun `commitAllDetached commits everything after the scope is cancelled`() =
    runBlocking {
      val scope = CoroutineScope(Job())
      val controller = controller(scope)
      var commits = 0
      controller.markPending("a", setOf("1"), "m") { commits++ }
      controller.markPending("b", setOf("2"), "m") { commits++ }
      scope.cancel()

      controller.commitAllDetached(Dispatchers.Unconfined)

      assertEquals(2, commits)
    }

  @Test
  fun `re-marking the same key commits the earlier batch and does not drop it`() {
    val controller = controller()
    val committed = mutableListOf<String>()
    controller.markPending("k", setOf("1"), "m") { committed += "first" }
    controller.markPending("k", setOf("2"), "m") { committed += "second" }

    assertEquals(listOf("first"), committed)
    assertEquals(setOf("2"), controller.pendingIds.value)
  }

  @Test
  fun `stale snackbar timeout does not commit the newer batch`() =
    runBlocking {
      val controller = controller()
      var secondCommits = 0
      controller.markPending("k", setOf("1"), "m") { }
      val stale = controller.currentEvent()
      controller.markPending("k", setOf("2"), "m") { secondCommits++ }

      controller.commit(stale)

      assertEquals(0, secondCommits)
      assertEquals(setOf("2"), controller.pendingIds.value)
    }

  @Test
  fun `unresolved event is replayed to a newly attached host`() =
    runBlocking {
      val controller = controller()
      controller.markPending("k", setOf("1"), "Deleted") { }
      val firstHost = controller.currentEvent()

      val secondHost = controller.currentEvent()

      assertEquals(firstHost, secondHost)
      assertEquals(setOf("1"), controller.pendingIds.value)
    }
}
