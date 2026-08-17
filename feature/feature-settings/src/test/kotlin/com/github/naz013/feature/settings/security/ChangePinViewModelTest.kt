package com.github.naz013.feature.settings.security

import com.github.naz013.testing.BaseTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ChangePinViewModelTest : BaseTest() {
  private val prefs = mockk<SecuritySettingsPreferences>(relaxed = true)

  private lateinit var viewModel: ChangePinViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.pinCode } returns "123456"

    viewModel = ChangePinViewModel(prefs = prefs)
  }

  private fun enter(digits: String) {
    digits.forEach { viewModel.onDigitClick(it.digitToInt()) }
  }

  @Test
  fun `initial state starts on OLD stage with an empty pin`() {
    assertEquals(ChangePinStage.OLD, viewModel.state.value.stage)
    assertEquals("", viewModel.state.value.pin)
  }

  @Test
  fun `onDigitClick appends a digit while below the required length`() {
    viewModel.onDigitClick(4)

    assertEquals("4", viewModel.state.value.pin)
    assertEquals(ChangePinStage.OLD, viewModel.state.value.stage)
  }

  @Test
  fun `onDigitClick moves from OLD to INPUT when the current pin matches prefs`() {
    enter("123456")

    assertEquals(ChangePinStage.INPUT, viewModel.state.value.stage)
    assertEquals("", viewModel.state.value.pin)
    assertNull(viewModel.navigationEvent.value)
  }

  @Test
  fun `onDigitClick emits ShowPinMismatch and stays on OLD stage when the current pin is wrong`() {
    enter("000000")

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(ChangePinEvent.ShowPinMismatch, event)
    assertEquals(ChangePinStage.OLD, viewModel.state.value.stage)
    assertEquals("", viewModel.state.value.pin)
  }

  @Test
  fun `onDigitClick moves from INPUT to REPEAT once six new digits are entered`() {
    enter("123456")

    enter("111111")

    assertEquals(ChangePinStage.REPEAT, viewModel.state.value.stage)
    assertEquals("", viewModel.state.value.pin)
  }

  @Test
  fun `onDigitClick saves the new pin and emits PinSaved when the repeat matches`() {
    enter("123456")
    enter("111111")

    enter("111111")

    verify { prefs.pinCode = "111111" }
    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(ChangePinEvent.PinSaved, event)
  }

  @Test
  fun `onDigitClick emits ShowPinMismatch and restarts at INPUT when the repeat does not match`() {
    enter("123456")
    enter("111111")

    enter("222222")

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(ChangePinEvent.ShowPinMismatch, event)
    assertEquals(ChangePinStage.INPUT, viewModel.state.value.stage)
    assertEquals("", viewModel.state.value.pin)
    verify(exactly = 0) { prefs.pinCode = any() }
  }

  @Test
  fun `onDeleteClick clears the pin`() {
    viewModel.onDigitClick(1)
    viewModel.onDigitClick(2)

    viewModel.onDeleteClick()

    assertEquals("", viewModel.state.value.pin)
  }
}
