package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.compose.foundation.DeviceScreenConfiguration
import com.github.naz013.ui.common.compose.foundation.deviceScreenConfiguration

/**
 * Shared shell for a full-screen "notification action" dialog (reminder, birthday, ...):
 * a [Scaffold] + [Surface], an early return while [state] is still loading, and a
 * portrait/landscape layout switch based on [deviceScreenConfiguration]. [overlayContent]
 * renders as a sibling of the surface, for content such as a modal bottom sheet that
 * shouldn't be clipped to it.
 */
@Composable
fun <S> ActionDialogScaffold(
  modifier: Modifier = Modifier,
  state: S?,
  logTag: String,
  overlayContent: @Composable () -> Unit = {},
  portrait: @Composable (S) -> Unit,
  landscape: @Composable (S) -> Unit,
) {
  val screenConfiguration = deviceScreenConfiguration()

  Scaffold { paddingValues ->
    Surface(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues),
      color = MaterialTheme.colorScheme.background,
    ) {
      val screenState = state ?: return@Surface

      Logger.d(logTag, "Rendering screen with configuration: $screenConfiguration")

      when (screenConfiguration) {
        DeviceScreenConfiguration.MobileLandscape -> landscape(screenState)
        else -> portrait(screenState)
      }
    }

    overlayContent()
  }
}
