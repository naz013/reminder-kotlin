package com.github.naz013.feature.settings.security

import com.github.naz013.testing.BaseTest
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AddPinViewModelTest : BaseTest() {
  private val prefs = mockk<SecuritySettingsPreferences>(relaxed = true)

  private lateinit var viewModel: AddPinViewModel

  @Before
  override fun setUp() {
    super.setUp()

    viewModel = AddPinViewModel(prefs = prefs)
  }

  @Test
  fun `initial state starts on INPUT stage with an empty pin`() {
    assertEquals(AddPinStage.INPUT, viewModel.state.value.stage)
    assertEquals("", viewModel.state.value.pin)
  }

  @Test
  fun `onDigitClick appends a digit while below the required length`() {
    viewModel.onDigitClick(1)

    assertEquals("1", viewModel.state.value.pin)
    assertEquals(AddPinStage.INPUT, viewModel.state.value.stage)
  }

  @Test
  fun `onDigitClick accumulates several digits in order`() {
    viewModel.onDigitClick(1)
    viewModel.onDigitClick(2)
    viewModel.onDigitClick(3)

    assertEquals("123", viewModel.state.value.pin)
  }

  @Test
  fun `onDigitClick moves from INPUT to REPEAT once six digits are entered`() {
    "123456".forEach { viewModel.onDigitClick(it.digitToInt()) }

    assertEquals(AddPinStage.REPEAT, viewModel.state.value.stage)
    assertEquals("", viewModel.state.value.pin)
    assertNull(viewModel.navigationEvent.value)
  }

  @Test
  fun `onDigitClick saves the pin and emits PinSaved when the repeat matches`() {
    "123456".forEach { viewModel.onDigitClick(it.digitToInt()) }
    "123456".forEach { viewModel.onDigitClick(it.digitToInt()) }

    verify { prefs.pinCode = "123456" }
    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(AddPinEvent.PinSaved, event)
  }

  @Test
  fun `onDigitClick emits ShowPinMismatch and restarts at INPUT when the repeat does not match`() {
    "123456".forEach { viewModel.onDigitClick(it.digitToInt()) }
    "654321".forEach { viewModel.onDigitClick(it.digitToInt()) }

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(AddPinEvent.ShowPinMismatch, event)
    assertEquals(AddPinStage.INPUT, viewModel.state.value.stage)
    assertEquals("", viewModel.state.value.pin)
    verify(exactly = 0) { prefs.pinCode = any() }
  }

  @Test
  fun `onDigitClick after a mismatch restarts the whole flow from scratch`() {
    "123456".forEach { viewModel.onDigitClick(it.digitToInt()) }
    "654321".forEach { viewModel.onDigitClick(it.digitToInt()) }

    "111111".forEach { viewModel.onDigitClick(it.digitToInt()) }
    "111111".forEach { viewModel.onDigitClick(it.digitToInt()) }

    verify { prefs.pinCode = "111111" }
    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(AddPinEvent.PinSaved, event)
  }

  @Test
  fun `onDeleteClick clears the pin`() {
    viewModel.onDigitClick(1)
    viewModel.onDigitClick(2)

    viewModel.onDeleteClick()

    assertEquals("", viewModel.state.value.pin)
  }
}
