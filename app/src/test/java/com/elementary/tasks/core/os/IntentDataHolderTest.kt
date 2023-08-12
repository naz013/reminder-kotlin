package com.elementary.tasks.core.os

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

  @Test
  fun testHasKey_dataPresentWrongKey_shouldReturnFalse() {
    intentDataHolder.putData("A", Birthday())
    val result = intentDataHolder.hasKey("B")
    assertFalse(result)
  }

  @Test
  fun testHasKey_dataPresent_shouldReturnTrue() {
    intentDataHolder.putData("A", Birthday())
    val result = intentDataHolder.hasKey("A")
    assertTrue(result)
  }

  @Test
  fun testGet_noDataPresent_shouldReturnNull() {
    val result = intentDataHolder.get("A", Birthday::class.java)
    assertNull(result)
  }

  @Test
  fun testGet_dataPresentWrongType_shouldReturnNull() {
    intentDataHolder.putData("A", Reminder())
    val result = intentDataHolder.get("A", Birthday::class.java)
    assertNull(result)
  }

  @Test
  fun testGet_dataPresent_shouldReturnObject() {
    val obj = Birthday()
    intentDataHolder.putData("A", obj)
    val result = intentDataHolder.get("A", Birthday::class.java)
    assertEquals(obj, result)
  }
}
