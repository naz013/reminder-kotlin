package com.github.naz013.ui.common.compose.foundation

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

enum class DeviceScreenConfiguration {
    MobilePortrait,
    MobileLandscape,
    TabletPortrait,
    TabletLandscape,
    DesktopSmall,
    DesktopNormal;

    companion object {
        fun fromWindowSizeClass(windowSizeClass: WindowSizeClass): DeviceScreenConfiguration {
            return when {
                windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) &&
                        windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND) -> DesktopNormal
                windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
                        windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) -> DesktopSmall
                windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) &&
                        windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) -> TabletLandscape
                windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
                        windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND) -> TabletPortrait
                windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> MobileLandscape
                else -> MobilePortrait
            }
        }
    }
}

@Composable
fun deviceScreenConfiguration(): DeviceScreenConfiguration {
    return DeviceScreenConfiguration.fromWindowSizeClass(currentWindowAdaptiveInfo().windowSizeClass)
}

@Composable
fun isMobilePortraitScreen(): Boolean {
    return deviceScreenConfiguration() == DeviceScreenConfiguration.MobilePortrait
}

@Composable
fun isMobileLandscapeScreen(): Boolean {
    return deviceScreenConfiguration() == DeviceScreenConfiguration.MobileLandscape
}

@Composable
fun isMobileScreen(): Boolean {
    return isMobilePortraitScreen() || isMobileLandscapeScreen()
}

@Composable
fun isTabletPortraitScreen(): Boolean {
    return deviceScreenConfiguration() == DeviceScreenConfiguration.TabletPortrait
}

@Composable
fun isTabletLandscapeScreen(): Boolean {
    return deviceScreenConfiguration() == DeviceScreenConfiguration.TabletLandscape
}

@Composable
fun isTabletScreen(): Boolean {
    return isTabletPortraitScreen() || isTabletLandscapeScreen()
}

@Composable
fun isDesktopSmallScreen(): Boolean {
    return deviceScreenConfiguration() == DeviceScreenConfiguration.DesktopSmall
}

@Composable
fun isDesktopNormalScreen(): Boolean {
    return deviceScreenConfiguration() == DeviceScreenConfiguration.DesktopNormal
}

@Composable
fun isDesktopScreen(): Boolean {
    return isDesktopSmallScreen() || isDesktopNormalScreen()
}
