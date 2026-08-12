package com.github.naz013.ui.common.compose.foundation

import androidx.compose.runtime.Composable

@Composable
fun <T> dynamicParameter(
    mobilePortrait: @Composable () -> T, // Default for all screen sizes
    mobileLandscape: @Composable () -> T = mobilePortrait,
    tabletPortrait: @Composable () -> T = mobileLandscape,
    tabletLandscape: @Composable () -> T = tabletPortrait,
    desktopSmall: @Composable () -> T = tabletPortrait,
    desktopNormal: @Composable () -> T = tabletLandscape,
): T {
    val deviceScreenConfiguration = deviceScreenConfiguration()
    return when (deviceScreenConfiguration) {
        DeviceScreenConfiguration.MobilePortrait -> mobilePortrait()
        DeviceScreenConfiguration.MobileLandscape -> mobileLandscape()
        DeviceScreenConfiguration.TabletPortrait -> tabletPortrait()
        DeviceScreenConfiguration.TabletLandscape -> tabletLandscape()
        DeviceScreenConfiguration.DesktopSmall -> desktopSmall()
        DeviceScreenConfiguration.DesktopNormal -> desktopNormal()
    }
}
