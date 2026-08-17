package com.github.naz013.feature.settings.security

import com.github.naz013.testing.BaseTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class DisablePinViewModelTest : BaseTest() {
  private val prefs = mockk<SecuritySettingsPreferences>(relaxed = true)

  private lateinit var viewModel: DisablePinViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.pinCode } returns "123456"

    viewModel = DisablePinViewModel(prefs = prefs)
  }

  private fun enter(digits: String) {
    digits.forEach { viewModel.onDigitClick(it.digitToInt()) }
  }

  @Test
  fun `initial state has an empty pin`() {
    assertEquals("", viewModel.state.value.pin)
  }

  @Test
  fun `onDigitClick appends a digit while below the required length`() {
    viewModel.onDigitClick(7)

    assertEquals("7", viewModel.state.value.pin)
  }

  @Test
  fun `onDigitClick clears the stored pin and emits PinCleared when the entry matches`() {
    enter("123456")

    verify { prefs.pinCode = "" }
    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(DisablePinEvent.PinCleared, event)
    // The matching branch never calls state.update - `pin` is left at its pre-final-digit value.
    assertEquals("12345", viewModel.state.value.pin)
  }

  @Test
  fun `onDigitClick emits ShowPinMismatch and clears the field when the entry is wrong`() {
    enter("000000")

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(DisablePinEvent.ShowPinMismatch, event)
    assertEquals("", viewModel.state.value.pin)
    verify(exactly = 0) { prefs.pinCode = "" }
  }

  @Test
  fun `onDigitClick can be retried after a mismatch`() {
    enter("000000")

    enter("123456")

    verify { prefs.pinCode = "" }
    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(DisablePinEvent.PinCleared, event)
  }

  @Test
  fun `onDeleteClick clears the pin`() {
    viewModel.onDigitClick(1)
    viewModel.onDigitClick(2)

    viewModel.onDeleteClick()

    assertEquals("", viewModel.state.value.pin)
  }

  @Test
  fun `no navigation event is emitted before six digits are entered`() {
    enter("1234")

    assertNull(viewModel.navigationEvent.value)
  }
}
