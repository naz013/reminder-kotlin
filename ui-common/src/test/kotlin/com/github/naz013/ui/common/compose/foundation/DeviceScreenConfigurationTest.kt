package com.github.naz013.ui.common.compose.foundation

import androidx.window.core.layout.WindowSizeClass
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceScreenConfigurationTest {

  @Test
  fun `returns MobilePortrait for a narrow short window`() {
    val result = DeviceScreenConfiguration.fromWindowSizeClass(WindowSizeClass(400, 800))

    assertEquals(DeviceScreenConfiguration.MobilePortrait, result)
  }

  @Test
  fun `returns MobileLandscape for a medium width but very short window`() {
    val result = DeviceScreenConfiguration.fromWindowSizeClass(WindowSizeClass(650, 400))

    assertEquals(DeviceScreenConfiguration.MobileLandscape, result)
  }

  @Test
  fun `returns TabletPortrait for a medium width and expanded height window`() {
    val result = DeviceScreenConfiguration.fromWindowSizeClass(WindowSizeClass(650, 950))

    assertEquals(DeviceScreenConfiguration.TabletPortrait, result)
  }

  @Test
  fun `returns TabletLandscape for an expanded width and medium height window`() {
    val result = DeviceScreenConfiguration.fromWindowSizeClass(WindowSizeClass(900, 600))

    assertEquals(DeviceScreenConfiguration.TabletLandscape, result)
  }

  @Test
  fun `returns DesktopSmall for a medium width and medium height window`() {
    val result = DeviceScreenConfiguration.fromWindowSizeClass(WindowSizeClass(700, 600))

    assertEquals(DeviceScreenConfiguration.DesktopSmall, result)
  }

  @Test
  fun `returns DesktopNormal for an expanded width and expanded height window`() {
    val result = DeviceScreenConfiguration.fromWindowSizeClass(WindowSizeClass(900, 950))

    assertEquals(DeviceScreenConfiguration.DesktopNormal, result)
  }
}
