package com.github.naz013.ui.common.compose.foundation

import androidx.compose.runtime.Composable

@Composable
fun DynamicScreen(
    mobilePortrait: @Composable () -> Unit, // Default for all screen sizes
    mobileLandscape: @Composable () -> Unit = mobilePortrait,
    tabletPortrait: @Composable () -> Unit = mobileLandscape,
    tabletLandscape: @Composable () -> Unit = tabletPortrait,
    desktopSmall: @Composable () -> Unit = tabletPortrait,
    desktopNormal: @Composable () -> Unit = tabletLandscape,
) {
    val deviceScreenConfiguration = deviceScreenConfiguration()
    when (deviceScreenConfiguration) {
        DeviceScreenConfiguration.MobilePortrait -> mobilePortrait()
        DeviceScreenConfiguration.MobileLandscape -> mobileLandscape()
        DeviceScreenConfiguration.TabletPortrait -> tabletPortrait()
        DeviceScreenConfiguration.TabletLandscape -> tabletLandscape()
        DeviceScreenConfiguration.DesktopSmall -> desktopSmall()
        DeviceScreenConfiguration.DesktopNormal -> desktopNormal()
    }
}
