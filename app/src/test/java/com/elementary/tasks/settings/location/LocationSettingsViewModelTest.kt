package com.elementary.tasks.settings.location

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.simplemap.MapStyle
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.SystemInfo
import com.github.naz013.ui.common.theme.ThemeProvider
import com.google.android.gms.maps.GoogleMap
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocationSettingsViewModelTest : BaseTest() {
  private val prefs = mockk<Prefs>()
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val themeProvider = mockk<ThemeProvider>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val systemInfo = mockk<SystemInfo>()
  private val mapStyle = mockk<MapStyle>()

  private lateinit var viewModel: LocationSettingsViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.isDistanceNotificationEnabled } returns false
    every { prefs.isDistanceNotificationEnabled = any() } just Runs
    every { prefs.radius } returns 500
    every { prefs.radius = any() } just Runs
    every { prefs.mapType } returns GoogleMap.MAP_TYPE_NORMAL
    every { prefs.mapType = any() } just Runs
    every { prefs.markerStyle } returns 0
    every { prefs.markerStyle = any() } just Runs
    every { prefs.trackTime } returns 30
    every { prefs.trackTime = any() } just Runs
    every { prefs.hapticsEnabled } returns false
    every { prefs.useMetric } returns true
    every { themeProvider.mapStylePreview } returns 111
    every { themeProvider.styleName } returns 222
    every { themeProvider.getMarkerLightColor(any()) } returns 0xff0000
    every { systemInfo.hasLocation } returns true
    every { mapStyle.colorsForSlider() } returns emptyList()

    viewModel =
      LocationSettingsViewModel(
        prefs = prefs,
        textProvider = textProvider,
        themeProvider = themeProvider,
        analyticsEventSender = analyticsEventSender,
        systemInfo = systemInfo,
        mapStyle = mapStyle,
      )
  }

  @Test
  fun `sends screen used analytics event on creation`() {
    verify { analyticsEventSender.send(any()) }
  }

  @Test
  fun `builds initial state from prefs and theme provider`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals(false, state.isNotificationChecked)
      assertEquals(111, state.mapStylePreviewRes)
      assertTrue(state.isMapStyleRowEnabled)
      assertTrue(state.hasLocation)
      assertNull(state.dialog)
    }

  @Test
  fun `onNotificationToggle flips the notification pref`() =
    runTest {
      viewModel.onNotificationToggle()

      verify { prefs.isDistanceNotificationEnabled = true }
    }

  @Test
  fun `onRadiusClick shows the radius dialog with current pref value`() =
    runTest {
      viewModel.onRadiusClick()

      val dialog = viewModel.state.first().dialog as LocationSettingsDialog.Radius
      assertEquals(500, dialog.value)
    }

  @Test
  fun `onRadiusPreviewChange updates the dialog preview value`() =
    runTest {
      viewModel.onRadiusClick()

      viewModel.onRadiusPreviewChange(750)

      val dialog = viewModel.state.first().dialog as LocationSettingsDialog.Radius
      assertEquals(750, dialog.value)
    }

  @Test
  fun `onRadiusPreviewChange posts haptic feedback when haptics enabled and value changes`() =
    runTest {
      every { prefs.hapticsEnabled } returns true
      viewModel.onRadiusClick()

      viewModel.onRadiusPreviewChange(750)

      val event = viewModel.navigationEvent.value?.peekContent()
      assertEquals(LocationSettingsEvent.HapticFeedback, event)
    }

  @Test
  fun `onRadiusPreviewChange does not post haptic feedback when value unchanged`() =
    runTest {
      every { prefs.hapticsEnabled } returns true
      viewModel.onRadiusClick()

      viewModel.onRadiusPreviewChange(500)

      assertNull(viewModel.navigationEvent.value)
    }

  @Test
  fun `onRadiusPreviewChange is a no-op when radius dialog is not shown`() =
    runTest {
      viewModel.onRadiusPreviewChange(750)

      assertNull(viewModel.state.first().dialog)
    }

  @Test
  fun `onRadiusConfirm saves the dialog value and dismisses`() =
    runTest {
      viewModel.onRadiusClick()
      viewModel.onRadiusPreviewChange(900)

      viewModel.onRadiusConfirm()

      verify { prefs.radius = 900 }
      assertNull(viewModel.state.first().dialog)
    }

  @Test
  fun `onRadiusConfirm is a no-op when radius dialog is not shown`() =
    runTest {
      viewModel.onRadiusConfirm()

      verify(exactly = 0) { prefs.radius = any() }
    }

  @Test
  fun `onMapTypeClick shows the map type dialog with current selection`() =
    runTest {
      every { prefs.mapType } returns GoogleMap.MAP_TYPE_SATELLITE

      viewModel.onMapTypeClick()

      val dialog = viewModel.state.first().dialog as LocationSettingsDialog.MapType
      assertEquals(1, dialog.selectedIndex)
    }

  @Test
  fun `onMapTypeOptionSelected saves the map type and dismisses`() =
    runTest {
      viewModel.onMapTypeClick()

      viewModel.onMapTypeOptionSelected(2)

      verify { prefs.mapType = 3 }
      assertNull(viewModel.state.first().dialog)
    }

  @Test
  fun `onMapStyleClick posts OpenMapStyle navigation event`() {
    viewModel.onMapStyleClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(LocationSettingsEvent.OpenMapStyle, event)
  }

  @Test
  fun `onMarkerStyleClick posts ShowMarkerColorPicker with prefs-derived values`() {
    every { prefs.markerStyle } returns 3
    every { prefs.hapticsEnabled } returns true
    every { mapStyle.colorsForSlider() } returns emptyList()

    viewModel.onMarkerStyleClick()

    val event = viewModel.navigationEvent.value?.peekContent() as LocationSettingsEvent.ShowMarkerColorPicker
    assertEquals(3, event.currentColorIndex)
    assertTrue(event.hapticFeedbackEnabled)
  }

  @Test
  fun `onMarkerColorSelected saves the selected marker style pref`() =
    runTest {
      viewModel.onMarkerColorSelected(5)

      verify { prefs.markerStyle = 5 }
    }

  @Test
  fun `onTrackerClick shows the tracker dialog with current pref value`() =
    runTest {
      viewModel.onTrackerClick()

      val dialog = viewModel.state.first().dialog as LocationSettingsDialog.Tracker
      assertEquals(30, dialog.seconds)
    }

  @Test
  fun `onTrackerPreviewChange updates the dialog preview seconds`() =
    runTest {
      viewModel.onTrackerClick()

      viewModel.onTrackerPreviewChange(60)

      val dialog = viewModel.state.first().dialog as LocationSettingsDialog.Tracker
      assertEquals(60, dialog.seconds)
    }

  @Test
  fun `onTrackerConfirm saves the dialog value and dismisses`() =
    runTest {
      viewModel.onTrackerClick()
      viewModel.onTrackerPreviewChange(90)

      viewModel.onTrackerConfirm()

      verify { prefs.trackTime = 90 }
      assertNull(viewModel.state.first().dialog)
    }

  @Test
  fun `onPlacesClick posts OpenPlaces navigation event`() {
    viewModel.onPlacesClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(LocationSettingsEvent.OpenPlaces, event)
  }

  @Test
  fun `onDialogDismiss clears the dialog`() =
    runTest {
      viewModel.onRadiusClick()

      viewModel.onDialogDismiss()

      assertNull(viewModel.state.first().dialog)
    }
}
