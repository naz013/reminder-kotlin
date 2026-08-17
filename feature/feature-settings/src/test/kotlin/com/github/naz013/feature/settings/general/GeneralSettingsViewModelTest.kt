package com.github.naz013.feature.settings.general

import androidx.appcompat.app.AppCompatDelegate
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.theme.ThemeModeHolder
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GeneralSettingsViewModelTest : BaseTest() {
  private val prefs = mockk<GeneralSettingsPreferences>()
  private val textProvider = mockk<TextProvider>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val themeModeHolder = mockk<ThemeModeHolder>(relaxed = true)

  private var appLanguage = 0
  private var nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
  private var hourFormat = 0
  private var useMetric = false
  private var analyticsEnabled = false
  private var hapticFeedbackEnabled = false

  private lateinit var viewModel: GeneralSettingsViewModel

  @Before
  override fun setUp() {
    super.setUp()

    every { prefs.appLanguage } answers { appLanguage }
    every { prefs.appLanguage = any() } answers { appLanguage = firstArg() }
    every { prefs.nightMode } answers { nightMode }
    every { prefs.nightMode = any() } answers { nightMode = firstArg() }
    every { prefs.hourFormat } answers { hourFormat }
    every { prefs.hourFormat = any() } answers { hourFormat = firstArg() }
    every { prefs.useMetric } answers { useMetric }
    every { prefs.useMetric = any() } answers { useMetric = firstArg() }
    every { prefs.analyticsEnabled } answers { analyticsEnabled }
    every { prefs.analyticsEnabled = any() } answers { analyticsEnabled = firstArg() }
    every { prefs.hapticsEnabled } answers { hapticFeedbackEnabled }
    every { prefs.hapticsEnabled = any() } answers { hapticFeedbackEnabled = firstArg() }

    every { textProvider.getStringArray(R.array.app_languages) } returns
      arrayOf("System default", "English", "German")
    every { textProvider.getString(R.string.light) } returns "Light"
    every { textProvider.getString(R.string.dark) } returns "Dark"
    every { textProvider.getString(R.string.system_default) } returns "System default"
    every { textProvider.getString(R.string.use_24_hour_format) } returns "24-hour"
    every { textProvider.getString(R.string.use_12_hour_format) } returns "12-hour"
    every { textProvider.getString(R.string.application_language) } returns "Language"
    every { textProvider.getString(R.string.theme) } returns "Theme"
    every { textProvider.getString(R.string._24_hour_time_format) } returns "24-hour time format"

    mockkStatic(AppCompatDelegate::class)
    every { AppCompatDelegate.setApplicationLocales(any()) } just Runs

    viewModel = GeneralSettingsViewModel(prefs, textProvider, analyticsEventSender, themeModeHolder)
  }

  @After
  override fun tearDown() {
    super.tearDown()
    unmockkStatic(AppCompatDelegate::class)
  }

  @Test
  fun `sends screen analytics event on init`() {
    verify { analyticsEventSender.send(ScreenUsedEvent(Screen.GENERAL_SETTINGS)) }
  }

  @Test
  fun `builds initial state from prefs`() {
    val state = viewModel.state.value

    assertEquals("System default", state.languageName)
    assertEquals("System default", state.themeName)
    assertEquals("System default", state.timeFormatName)
    assertEquals(false, state.isMetricChecked)
    assertEquals(false, state.isAnalyticsChecked)
    assertNull(state.dialog)
  }

  @Test
  fun `onLanguageClick shows language dialog with current selection`() {
    appLanguage = 1

    viewModel.onLanguageClick()

    val dialog = viewModel.state.value.dialog as GeneralSettingsDialog.Language
    assertEquals("Language", dialog.title)
    assertEquals(listOf("System default", "English", "German"), dialog.options)
    assertEquals(1, dialog.selectedIndex)
  }

  @Test
  fun `onThemeClick shows theme dialog with current selection`() {
    nightMode = AppCompatDelegate.MODE_NIGHT_YES

    viewModel.onThemeClick()

    val dialog = viewModel.state.value.dialog as GeneralSettingsDialog.Theme
    assertEquals("Theme", dialog.title)
    assertEquals(listOf("Light", "Dark", "System default"), dialog.options)
    assertEquals(1, dialog.selectedIndex)
  }

  @Test
  fun `onTimeFormatClick shows time format dialog with current selection`() {
    hourFormat = 2

    viewModel.onTimeFormatClick()

    val dialog = viewModel.state.value.dialog as GeneralSettingsDialog.TimeFormat
    assertEquals("24-hour time format", dialog.title)
    assertEquals(listOf("System default", "24-hour", "12-hour"), dialog.options)
    assertEquals(2, dialog.selectedIndex)
  }

  @Test
  fun `onDialogDismiss clears the dialog`() {
    viewModel.onThemeClick()

    viewModel.onDialogDismiss()

    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `selecting a language option persists it, recreates locales and dismisses dialog`() {
    viewModel.onLanguageClick()

    viewModel.onDialogOptionSelected(2)

    assertEquals(2, appLanguage)
    assertNull(viewModel.state.value.dialog)
    assertEquals("German", viewModel.state.value.languageName)
    val event = viewModel.event.value?.peekContent()
    assertEquals(GeneralSettingsEvent.RestartApp, event)
  }

  @Test
  fun `selecting the same language does not post a recreate event`() {
    appLanguage = 0
    viewModel.onLanguageClick()

    viewModel.onDialogOptionSelected(0)

    assertEquals(0, appLanguage)
    assertNull(viewModel.event.value)
  }

  @Test
  fun `selecting a theme option persists mode and updates theme holder`() {
    viewModel.onThemeClick()

    viewModel.onDialogOptionSelected(1)

    assertEquals(AppCompatDelegate.MODE_NIGHT_YES, nightMode)
    verify { themeModeHolder.nightMode = AppCompatDelegate.MODE_NIGHT_YES }
    assertEquals("Dark", viewModel.state.value.themeName)
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `selecting a time format option persists it`() {
    viewModel.onTimeFormatClick()

    viewModel.onDialogOptionSelected(1)

    assertEquals(1, hourFormat)
    assertEquals("24-hour", viewModel.state.value.timeFormatName)
  }

  @Test
  fun `onDialogOptionSelected with no dialog open is a no-op`() {
    viewModel.onDialogOptionSelected(1)

    assertEquals(0, appLanguage)
    assertEquals(0, hourFormat)
    assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, nightMode)
  }

  @Test
  fun `onMetricToggle flips the metric flag and persists it`() {
    useMetric = false

    viewModel.onMetricToggle()

    assertEquals(true, useMetric)
    assertEquals(true, viewModel.state.value.isMetricChecked)
  }

  @Test
  fun `onAnalyticsToggle flips analytics flag, persists it and updates collection state`() {
    analyticsEnabled = false

    viewModel.onAnalyticsToggle()

    assertEquals(true, analyticsEnabled)
    assertEquals(true, viewModel.state.value.isAnalyticsChecked)
    verify { analyticsEventSender.setCollectionEnabled(true) }
  }
}
