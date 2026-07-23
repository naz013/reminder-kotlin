package com.elementary.tasks.settings

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class NoteSettingsViewModelTest : BaseTest() {
  private val prefs = mockk<Prefs>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)

  private var colorRememberingEnabled = false
  private var fontSizeRememberingEnabled = false
  private var fontStyleRememberingEnabled = false
  private var noteColorOpacity = 100
  private var hapticsEnabled = true

  private lateinit var viewModel: NoteSettingsViewModel

  @Before
  override fun setUp() {
    super.setUp()

    every { prefs.isNoteColorRememberingEnabled } answers { colorRememberingEnabled }
    every { prefs.isNoteColorRememberingEnabled = any() } answers { colorRememberingEnabled = firstArg() }
    every { prefs.isNoteFontSizeRememberingEnabled } answers { fontSizeRememberingEnabled }
    every { prefs.isNoteFontSizeRememberingEnabled = any() } answers { fontSizeRememberingEnabled = firstArg() }
    every { prefs.isNoteFontStyleRememberingEnabled } answers { fontStyleRememberingEnabled }
    every { prefs.isNoteFontStyleRememberingEnabled = any() } answers { fontStyleRememberingEnabled = firstArg() }
    every { prefs.noteColorOpacity } answers { noteColorOpacity }
    every { prefs.noteColorOpacity = any() } answers { noteColorOpacity = firstArg() }
    every { prefs.hapticsEnabled } answers { hapticsEnabled }

    viewModel = NoteSettingsViewModel(prefs, analyticsEventSender)
  }

  @Test
  fun `sends screen analytics event on init`() {
    verify { analyticsEventSender.send(ScreenUsedEvent(Screen.NOTE_SETTINGS)) }
  }

  @Test
  fun `builds initial state from prefs`() {
    colorRememberingEnabled = true
    fontSizeRememberingEnabled = false
    fontStyleRememberingEnabled = true
    noteColorOpacity = 42
    hapticsEnabled = false
    viewModel = NoteSettingsViewModel(prefs, analyticsEventSender)

    val state = viewModel.state.value

    assertEquals(true, state.isColorRememberChecked)
    assertEquals(false, state.isFontSizeRememberChecked)
    assertEquals(true, state.isFontStyleRememberChecked)
    assertEquals(42, state.colorOpacity)
    assertEquals(false, state.hapticFeedbackEnabled)
    assertNull(state.opacityDialog)
  }

  @Test
  fun `onColorRememberToggle flips the flag and persists it`() {
    colorRememberingEnabled = false

    viewModel.onColorRememberToggle()

    assertEquals(true, colorRememberingEnabled)
    assertEquals(true, viewModel.state.value.isColorRememberChecked)
  }

  @Test
  fun `onFontSizeRememberToggle flips the flag and persists it`() {
    fontSizeRememberingEnabled = false

    viewModel.onFontSizeRememberToggle()

    assertEquals(true, fontSizeRememberingEnabled)
    assertEquals(true, viewModel.state.value.isFontSizeRememberChecked)
  }

  @Test
  fun `onFontStyleRememberToggle flips the flag and persists it`() {
    fontStyleRememberingEnabled = false

    viewModel.onFontStyleRememberToggle()

    assertEquals(true, fontStyleRememberingEnabled)
    assertEquals(true, viewModel.state.value.isFontStyleRememberChecked)
  }

  @Test
  fun `onOpacityClick opens the opacity dialog with the current preference value`() {
    noteColorOpacity = 55

    viewModel.onOpacityClick()

    assertEquals(55, viewModel.state.value.opacityDialog?.previewValue)
  }

  @Test
  fun `onOpacityPreviewChange updates only the preview value without persisting`() {
    viewModel.onOpacityClick()

    viewModel.onOpacityPreviewChange(80)

    assertEquals(80, viewModel.state.value.opacityDialog?.previewValue)
    assertEquals(100, noteColorOpacity)
  }

  @Test
  fun `onOpacityPreviewChange without an open dialog is a no-op`() {
    viewModel.onOpacityPreviewChange(80)

    assertNull(viewModel.state.value.opacityDialog)
  }

  @Test
  fun `onOpacityConfirm persists the preview value and closes the dialog`() {
    viewModel.onOpacityClick()
    viewModel.onOpacityPreviewChange(80)

    viewModel.onOpacityConfirm()

    assertEquals(80, noteColorOpacity)
    assertEquals(80, viewModel.state.value.colorOpacity)
    assertNull(viewModel.state.value.opacityDialog)
  }

  @Test
  fun `onOpacityConfirm without an open dialog does not persist anything`() {
    viewModel.onOpacityConfirm()

    assertEquals(100, noteColorOpacity)
    assertNull(viewModel.state.value.opacityDialog)
  }

  @Test
  fun `onOpacityDialogDismiss closes the dialog without persisting`() {
    viewModel.onOpacityClick()
    viewModel.onOpacityPreviewChange(80)

    viewModel.onOpacityDialogDismiss()

    assertNull(viewModel.state.value.opacityDialog)
    assertEquals(100, noteColorOpacity)
  }
}
