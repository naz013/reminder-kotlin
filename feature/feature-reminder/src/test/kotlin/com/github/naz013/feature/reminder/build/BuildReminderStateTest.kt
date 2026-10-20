package com.github.naz013.feature.reminder.build

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildReminderStateTest {

  @Test
  fun hasColor_isFalse_whenNoColorIndexIsSet() {
    val state = BuildReminderState(colorIndex = null)

    assertFalse(state.hasColor)
  }

  @Test
  fun hasColor_isTrue_whenColorIndexIsSet() {
    val state = BuildReminderState(colorIndex = 0)

    assertTrue(state.hasColor)
  }

  @Test
  fun colorPickerIndex_isNegativeOne_whenNoColorIsSelected() {
    val state = BuildReminderState(colorIndex = null)

    assertEquals(-1, state.colorPickerIndex)
  }

  @Test
  fun colorPickerIndex_matchesColorIndex_whenColorIsSelected() {
    val state = BuildReminderState(colorIndex = 5)

    assertEquals(5, state.colorPickerIndex)
  }
}
