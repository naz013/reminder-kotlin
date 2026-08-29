package com.github.naz013.ui.common.compose.foundation

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * On Medium+ width (tablet/desktop), the banner is pinned to this fixed width and centered instead
 * of stretching edge-to-edge across the whole window - see [InAppAlertBannerContent].
 */
private val IN_APP_ALERT_BANNER_MAX_WIDTH = 480.dp

/** One action button on [InAppAlertBanner] - e.g. "OK", "Snooze", "Call", "SMS". */
data class InAppAlertBannerAction(
  @DrawableRes val iconRes: Int,
  val label: String,
  val onClick: () -> Unit,
)

/** What [InAppAlertBanner] renders. `null` hides the banner entirely. */
data class InAppAlertBannerState(
  val id: String,
  val title: String,
  val text: String?,
  @DrawableRes val iconRes: Int,
  val actions: List<InAppAlertBannerAction>,
)

/**
 * In-app mirror of a just-posted reminder/birthday system notification, docked below the top app
 * bar while the app is foregrounded (see `docs/in-app-notification-overlay.md`). A new [state]
 * (different [InAppAlertBannerState.id]) replaces the previous one in place rather than stacking -
 * the caller (`InAppAlertViewModel` in the `app` module) is responsible for only ever surfacing the
 * most recently emitted alert.
 */
@Composable
fun InAppAlertBanner(
  modifier: Modifier = Modifier,
  state: InAppAlertBannerState?,
) {
  AnimatedContent(
    modifier =
    modifier
      .windowInsetsPadding(WindowInsets.statusBars)
      // Approximates the standard M3 TopAppBar height - there is no single shared Scaffold to
      // measure the real app bar against (every screen builds its own), so this is a deliberate
      // approximation rather than a per-screen measurement (see docs/in-app-notification-overlay.md).
      .padding(top = 56.dp),
    targetState = state,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "InAppAlertBanner",
  ) { targetState ->
    if (targetState != null) {
      InAppAlertBannerContent(targetState)
    }
  }
}

/**
 * Full width on Compact (phone) width. On Medium+ width (tablet/desktop), caps at
 * [IN_APP_ALERT_BANNER_MAX_WIDTH] instead of stretching edge-to-edge across the whole window -
 * paired with the [InAppAlertBanner] call site aligning this content `TopCenter` within its parent
 * `Box`, this centers the banner horizontally once it's narrower than the window.
 */
@Composable
private fun Modifier.inAppAlertBannerWidth(): Modifier =
  if (isTabletScreen() || isDesktopScreen()) {
    widthIn(max = IN_APP_ALERT_BANNER_MAX_WIDTH).fillMaxWidth()
  } else {
    fillMaxWidth()
  }

@Composable
private fun InAppAlertBannerContent(state: InAppAlertBannerState) {
  Surface(
    modifier =
    Modifier
      .inAppAlertBannerWidth()
      .padding(horizontal = 12.dp, vertical = 8.dp),
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    shadowElevation = 6.dp,
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          painter = painterResource(state.iconRes),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
          Text(
            text = state.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          if (state.text != null) {
            Text(
              text = state.text,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
      if (state.actions.isNotEmpty()) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
          horizontalArrangement = Arrangement.End,
        ) {
          state.actions.forEach { action ->
            TextButton(onClick = action.onClick) {
              Icon(
                painter = painterResource(action.iconRes),
                contentDescription = null,
                modifier = Modifier.padding(end = 4.dp),
              )
              Text(action.label)
            }
          }
        }
      }
    }
  }
}
