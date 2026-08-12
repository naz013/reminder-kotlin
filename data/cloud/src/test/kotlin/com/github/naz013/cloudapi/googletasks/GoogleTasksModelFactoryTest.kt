package com.github.naz013.cloudapi.googletasks

import com.github.naz013.domain.GoogleTask
import com.google.api.services.tasks.model.Task
import com.google.api.services.tasks.model.TaskList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GoogleTasksModelFactoryTest {
  private lateinit var factory: GoogleTasksModelFactory

  @Before
  fun setUp() {
    factory = GoogleTasksModelFactory(GetRandomGoogleTaskListColorUseCase())
  }

  @Test
  fun `toModel carries the title and only sets notes and due when present`() {
    val googleTask = GoogleTask(title = "Buy milk", notes = "", dueDate = 0L)

    val task = factory.toModel(googleTask)

    assertEquals("Buy milk", task.title)
    assertNull(task.notes)
    assertNull(task.due)
  }

  @Test
  fun `toModel sets notes and a formatted due date when present`() {
    val dueMillis = 1_700_000_000_000L
    val googleTask = GoogleTask(title = "Buy milk", notes = "2%", dueDate = dueMillis)

    val task = factory.toModel(googleTask)

    assertEquals("2%", task.notes)
    assertTrue(task.due.endsWith("Z"))
  }

  @Test
  fun `toDomain defaults missing fields to empty strings and false flags`() {
    val task = Task()

    val googleTask = factory.toDomain(task, listId = "list-1")

    assertEquals("list-1", googleTask.listId)
    assertEquals("", googleTask.title)
    assertEquals(0, googleTask.del)
    assertEquals(0, googleTask.hidden)
    assertTrue(googleTask.uploaded)
  }

  @Test
  fun `toDomain maps deleted and hidden flags to one`() {
    val task = Task().setDeleted(true).setHidden(true)

    val googleTask = factory.toDomain(task, listId = "list-1")

    assertEquals(1, googleTask.del)
    assertEquals(1, googleTask.hidden)
  }

  @Test
  fun `toRfc3339Format and back round trips to the same millisecond`() {
    val millis = 1_700_000_000_000L

    val formatted = factory.toRfc3339Format(millis)
    val task = Task().setDue(formatted)
    val googleTask = factory.toDomain(task, listId = "list-1")

    assertEquals(millis, googleTask.dueDate)
  }

  @Test
  fun `toRfc3339Format anchors the Z suffix to actual UTC, not the system default zone`() {
    // 2023-11-14T22:13:20.000Z
    val millis = 1_700_000_000_000L

    val formatted = factory.toRfc3339Format(millis)

    assertEquals("2023-11-14T22:13:20.000Z", formatted)
  }

  @Test
  fun `update overwrites fields from the api task and marks it uploaded`() {
    val existing = GoogleTask(title = "Old title", uploaded = false)
    val apiTask = Task().setTitle("New title").setId("task-1")

    val updated = factory.update(existing, apiTask)

    assertEquals("New title", updated.title)
    assertEquals("task-1", updated.taskId)
    assertTrue(updated.uploaded)
  }

  @Test
  fun `toDomain for TaskList defaults missing fields to empty strings`() {
    val taskList = TaskList()

    val result = factory.toDomain(taskList, color = 3)

    assertEquals("", result.title)
    assertEquals("", result.listId)
    assertEquals(3, result.color)
    assertTrue(result.uploaded)
  }

  @Test
  fun `update for TaskList overwrites fields and marks it uploaded`() {
    val existing = com.github.naz013.domain.GoogleTaskList(title = "Old", uploaded = false)
    val apiTaskList = TaskList().setTitle("New").setId("list-2")

    val updated = factory.update(existing, apiTaskList)

    assertEquals("New", updated.title)
    assertEquals("list-2", updated.listId)
    assertTrue(updated.uploaded)
    assertFalse(existing.uploaded)
  }
}
