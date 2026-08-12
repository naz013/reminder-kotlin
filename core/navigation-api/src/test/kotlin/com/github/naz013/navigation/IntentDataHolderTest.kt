package com.github.naz013.navigation

import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class IntentDataHolderTest {

  private lateinit var intentDataHolder: IntentDataHolder

  @Before
  fun setUp() {
    intentDataHolder = IntentDataHolder()
  }

  @Test
  fun testHasKey_noDataPresent_shouldReturnFalse() {
    val result = intentDataHolder.hasKey("A")
    assertFalse(result)
  }
}
