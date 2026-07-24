package com.github.naz013.feature.common.livedata

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventTest {

  @Test
  fun `getContentIfNotHandled returns the content the first time`() {
    val event = Event("payload")

    assertEquals("payload", event.getContentIfNotHandled())
  }

  @Test
  fun `getContentIfNotHandled returns null on subsequent calls`() {
    val event = Event("payload")
    event.getContentIfNotHandled()

    assertNull(event.getContentIfNotHandled())
  }

  @Test
  fun `peekContent returns the content every time without marking it handled`() {
    val event = Event("payload")

    assertEquals("payload", event.peekContent())
    assertEquals("payload", event.peekContent())
    assertEquals("payload", event.getContentIfNotHandled())
  }
}
