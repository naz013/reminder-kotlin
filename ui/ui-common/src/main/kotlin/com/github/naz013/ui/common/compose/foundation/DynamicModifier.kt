package com.github.naz013.ui.common.compose.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Modifier.dynamicModifier(
    mobilePortrait: @Composable (Modifier) -> Modifier, // Default for all screen sizes
    mobileLandscape: @Composable (Modifier) -> Modifier = mobilePortrait,
    tabletPortrait: @Composable (Modifier) -> Modifier = mobileLandscape,
    tabletLandscape: @Composable (Modifier) -> Modifier = tabletPortrait,
    desktopSmall: @Composable (Modifier) -> Modifier = tabletPortrait,
    desktopNormal: @Composable (Modifier) -> Modifier = tabletLandscape,
): Modifier {
    val deviceScreenConfiguration = deviceScreenConfiguration()
    return when (deviceScreenConfiguration) {
        DeviceScreenConfiguration.MobilePortrait -> mobilePortrait(this)
        DeviceScreenConfiguration.MobileLandscape -> mobileLandscape(this)
        DeviceScreenConfiguration.TabletPortrait -> tabletPortrait(this)
        DeviceScreenConfiguration.TabletLandscape -> tabletLandscape(this)
        DeviceScreenConfiguration.DesktopSmall -> desktopSmall(this)
        DeviceScreenConfiguration.DesktopNormal -> desktopNormal(this)
    }
}
