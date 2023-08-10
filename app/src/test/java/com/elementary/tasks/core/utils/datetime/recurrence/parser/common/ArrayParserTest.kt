package com.elementary.tasks.core.utils.datetime.recurrence.parser.common

import org.junit.Assert.assertEquals
import org.junit.Test

class ArrayParserTest {

  private val arrayParser = ArrayParser()

  @Test
  fun testEmptyArray() {
    val result = arrayParser.parse("")
    assertEquals(emptyList<String>(), result)
  }

  @Test
  fun testSingleElementArray() {
    val result = arrayParser.parse("19960402T010000Z")

    val expected = listOf(
      "19960402T010000Z"
    )
    assertEquals(expected, result)
  }

  @Test
  fun testMultipleElementsArray() {
    val result = arrayParser.parse("19960402T010000Z,19960403T010000Z,19960404T010000Z")

    val expected = listOf(
      "19960402T010000Z",
      "19960403T010000Z",
      "19960404T010000Z"
    )
    assertEquals(expected, result)
  }
}
