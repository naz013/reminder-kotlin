package com.github.naz013.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleTaskListTest {

  @Test
  fun `isDefault is true only when def equals one`() {
    assertTrue(GoogleTaskList(def = 1).isDefault())
    assertFalse(GoogleTaskList(def = 0).isDefault())
    assertFalse(GoogleTaskList(def = 2).isDefault())
  }
}
