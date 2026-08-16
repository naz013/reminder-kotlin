package com.github.naz013.feature.reminder.build.reminder

import com.github.naz013.domain.reminder.BiType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BiTypeToBiValueTest {
  private lateinit var subject: BiTypeToBiValue

  @Before
  fun setUp() {
    subject = BiTypeToBiValue()
  }

  @Test
  fun `test Repeat Interval conversion`() {
    val result = subject.invoke<Long>(BiType.REPEAT_INTERVAL, "55")

    println("result = $result")
  }

  @Test
  fun `parses category as an int`() {
    val result = subject.invoke<Int>(BiType.CATEGORY, "2")

    assertEquals(2, result)
  }

  @Test
  fun `parses lock screen visibility as an int`() {
    val result = subject.invoke<Int>(BiType.LOCK_SCREEN_VISIBILITY, "1")

    assertEquals(1, result)
  }

  @Test
  fun `parses delay minutes as an int`() {
    val result = subject.invoke<Int>(BiType.DELAY_MINUTES, "30")

    assertEquals(30, result)
  }

  @Test
  fun `parses bypass dnd as a boolean`() {
    val result = subject.invoke<Boolean>(BiType.BYPASS_DND, "true")

    assertEquals(true, result)
  }

  @Test
  fun `parses wake screen as a boolean`() {
    val result = subject.invoke<Boolean>(BiType.WAKE_SCREEN, "false")

    assertEquals(false, result)
  }

  @Test
  fun `parses vibration pattern as a long list`() {
    val result = subject.invoke<List<Long>>(BiType.VIBRATION_PATTERN, "0,200,150,200")

    assertEquals(listOf(0L, 200L, 150L, 200L), result)
  }
}
