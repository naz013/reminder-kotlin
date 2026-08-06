package com.github.naz013.feature.common

import android.database.Cursor
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CursorExtensionsTest {

  @Test
  fun `readString returns the column value when the column exists`() {
    val cursor = mockk<Cursor>()
    every { cursor.getColumnIndex("name") } returns 2
    every { cursor.getString(2) } returns "value"

    assertEquals("value", cursor.readString("name"))
  }

  @Test
  fun `readString returns null when the column is missing`() {
    val cursor = mockk<Cursor>()
    every { cursor.getColumnIndex("name") } returns -1

    assertNull(cursor.readString("name"))
  }

  @Test
  fun `readString with default falls back when the column is missing`() {
    val cursor = mockk<Cursor>()
    every { cursor.getColumnIndex("name") } returns -1

    assertEquals("fallback", cursor.readString("name", "fallback"))
  }

  @Test
  fun `readLong returns the column value when the column exists`() {
    val cursor = mockk<Cursor>()
    every { cursor.getColumnIndex("count") } returns 1
    every { cursor.getLong(1) } returns 42L

    assertEquals(42L, cursor.readLong("count"))
  }

  @Test
  fun `readLong with default falls back when the column is missing`() {
    val cursor = mockk<Cursor>()
    every { cursor.getColumnIndex("count") } returns -1

    assertEquals(7L, cursor.readLong("count", 7L))
  }

  @Test
  fun `readInt returns the column value when the column exists`() {
    val cursor = mockk<Cursor>()
    every { cursor.getColumnIndex("age") } returns 0
    every { cursor.getInt(0) } returns 30

    assertEquals(30, cursor.readInt("age"))
  }

  @Test
  fun `readInt with default falls back when the column is missing`() {
    val cursor = mockk<Cursor>()
    every { cursor.getColumnIndex("age") } returns -1

    assertEquals(-1, cursor.readInt("age", -1))
  }
}
