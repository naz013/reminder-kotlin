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

  @Test
  fun `markPending hides the ids and emits one undo event`() =
    runBlocking {
      val controller = controller()

      controller.markPending("k", setOf("1", "2"), "Deleted") { }

      assertEquals(setOf("1", "2"), controller.pendingIds.value)
      assertEquals(UndoEvent("k", "Deleted"), withTimeout(1000) { controller.undoEvents.first() })
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
    controller.markPending("b", setOf("2"), "m") { committedB++ }

    controller.undo("a")
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
}
