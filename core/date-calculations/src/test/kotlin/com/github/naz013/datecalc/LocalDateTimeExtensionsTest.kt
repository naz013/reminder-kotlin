package com.github.naz013.datecalc

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class LocalDateTimeExtensionsTest {

  @Test
  fun `minusMillis subtracts whole seconds`() {
    val start = LocalDateTime.of(2023, 6, 17, 12, 0, 30)

    val result = start.minusMillis(5000)

    assertEquals(LocalDateTime.of(2023, 6, 17, 12, 0, 25), result)
  }

  @Test
  fun `plusMillis adds whole seconds`() {
    val start = LocalDateTime.of(2023, 6, 17, 12, 0, 0)

    val result = start.plusMillis(5000)

    assertEquals(LocalDateTime.of(2023, 6, 17, 12, 0, 5), result)
  }

  @Test
  fun `plusMillis truncates sub-second remainders`() {
    val start = LocalDateTime.of(2023, 6, 17, 12, 0, 0)

    val result = start.plusMillis(1500)

    assertEquals(LocalDateTime.of(2023, 6, 17, 12, 0, 1), result)
  }
}
