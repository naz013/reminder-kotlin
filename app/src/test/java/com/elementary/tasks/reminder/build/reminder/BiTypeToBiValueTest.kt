package com.elementary.tasks.reminder.build.reminder

import com.github.naz013.domain.reminder.BiType
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
}
