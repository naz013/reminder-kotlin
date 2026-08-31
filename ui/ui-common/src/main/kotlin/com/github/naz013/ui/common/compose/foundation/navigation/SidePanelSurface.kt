package com.github.naz013.ui.common.compose.foundation.navigation

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Max width of a Material 3 side sheet - https://m3.material.io/components/side-sheets/specs. */
val SidePanelMaxWidth = 400.dp

private val SidePanelCornerRadius = 16.dp

/**
 * The elevated, rounded container for a Material 3 side sheet
 * (https://m3.material.io/components/side-sheets/overview) docked to the trailing edge of the
 * window - used to show a screen (e.g. build reminder, edit birthday) as a floating panel next to
 * a full-screen host (e.g. Calendar) on Medium+ width, instead of pushing it full-screen. Only the
 * leading corners are rounded since the panel sits flush against the trailing window edge.
 */
@Composable
fun SidePanelSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Surface(
    modifier = modifier.fillMaxHeight().width(SidePanelMaxWidth),
    shape = RoundedCornerShape(topStart = SidePanelCornerRadius, bottomStart = SidePanelCornerRadius),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    tonalElevation = 3.dp,
    content = content,
  )
}

private const val SidePanelHostKey = "com.github.naz013.ui.common.compose.foundation.navigation.SidePanelHost"
private const val SidePanelSupportingKey =
  "com.github.naz013.ui.common.compose.foundation.navigation.SidePanelSupporting"

/**
 * Marks a Nav3 entry as the full-screen host a side sheet can float over (e.g. Calendar - see
 * `CalendarNavGraph.kt`). Paired with [sidePanelSupporting] by `SidePanelSceneStrategy` (`app`
 * module) - a plain `Map<String, Any>` here since this module doesn't depend on Nav3.
 */
fun sidePanelHost(): Map<String, Any> = mapOf(SidePanelHostKey to true)

/**
 * Marks a Nav3 entry as one that, when pushed directly on top of a [sidePanelHost] entry on
 * Medium+ width, renders inside a [SidePanelSurface] floating over the host instead of pushing
 * full-screen (see `BuildReminderNavGraph.kt`/`BirthdaysNavGraph.kt`/`ReminderPreviewNavGraph.kt`).
 */
fun sidePanelSupporting(): Map<String, Any> = mapOf(SidePanelSupportingKey to true)

/** Whether [metadata] (a Nav3 `NavEntry.metadata`) carries the [sidePanelHost] tag. */
fun isSidePanelHost(metadata: Map<String, Any>): Boolean = metadata[SidePanelHostKey] == true

/** Whether [metadata] (a Nav3 `NavEntry.metadata`) carries the [sidePanelSupporting] tag. */
fun isSidePanelSupporting(metadata: Map<String, Any>): Boolean = metadata[SidePanelSupportingKey] == true
