package com.github.naz013.workapi

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDataTest {

  @Test
  fun `builder collects values by key and type`() {
    val data = TaskData.builder()
      .putString("name", "task")
      .putBoolean("enabled", true)
      .putStringArray("tags", arrayOf("a", "b"))
      .build()

    assertEquals("task", data.getString("name"))
    assertTrue(data.getBoolean("enabled"))
    assertArrayEquals(arrayOf("a", "b"), data.getStringArray("tags"))
  }

  @Test
  fun `getString returns null for a missing key`() {
    val data = TaskData.EMPTY

    assertNull(data.getString("missing"))
  }

  @Test
  fun `getBoolean returns the given default for a missing key`() {
    val data = TaskData.EMPTY

    assertFalse(data.getBoolean("missing"))
    assertTrue(data.getBoolean("missing", default = true))
  }

  @Test
  fun `getStringArray returns null for a missing key`() {
    val data = TaskData.EMPTY

    assertNull(data.getStringArray("missing"))
  }

  @Test
  fun `of filters out null values`() {
    val data = TaskData.of(mapOf("keep" to "value", "drop" to null))

    assertEquals("value", data.getString("keep"))
    assertEquals(setOf("keep"), data.asMap().keys)
  }

  @Test
  fun `EMPTY has no entries`() {
    assertTrue(TaskData.EMPTY.asMap().isEmpty())
  }
}
