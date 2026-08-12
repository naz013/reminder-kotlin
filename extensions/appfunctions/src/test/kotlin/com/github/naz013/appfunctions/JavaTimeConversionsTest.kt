package com.github.naz013.appfunctions

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate as JavaLocalDate
import java.time.LocalDateTime as JavaLocalDateTime
import org.threeten.bp.LocalDate as ThreeTenLocalDate
import org.threeten.bp.LocalDateTime as ThreeTenLocalDateTime

class JavaTimeConversionsTest {

  @Test
  fun `LocalDateTime converts from java time to threeten preserving all fields`() {
    val javaDateTime = JavaLocalDateTime.of(2026, 8, 1, 9, 30, 15)

    val result = javaDateTime.toThreeTen()

    assertEquals(ThreeTenLocalDateTime.of(2026, 8, 1, 9, 30, 15), result)
  }

  @Test
  fun `LocalDateTime converts from threeten to java time preserving all fields`() {
    val threeTenDateTime = ThreeTenLocalDateTime.of(2026, 8, 1, 9, 30, 15)

    val result = threeTenDateTime.toJavaTime()

    assertEquals(JavaLocalDateTime.of(2026, 8, 1, 9, 30, 15), result)
  }

  @Test
  fun `LocalDate converts from java time to threeten preserving all fields`() {
    val javaDate = JavaLocalDate.of(1999, 10, 3)

    val result = javaDate.toThreeTen()

    assertEquals(ThreeTenLocalDate.of(1999, 10, 3), result)
  }

  @Test
  fun `LocalDate converts from threeten to java time preserving all fields`() {
    val threeTenDate = ThreeTenLocalDate.of(1999, 10, 3)

    val result = threeTenDate.toJavaTime()

    assertEquals(JavaLocalDate.of(1999, 10, 3), result)
  }
}
