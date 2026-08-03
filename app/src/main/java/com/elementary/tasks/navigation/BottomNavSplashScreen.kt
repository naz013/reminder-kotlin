package com.elementary.tasks.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.AnimatedGradientBackground
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BottomNavSplashScreen(modifier: Modifier = Modifier) {
  var nameVisible by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    delay(NAME_APPEARANCE_DELAY_MILLIS.milliseconds)
    nameVisible = true
  }

  AnimatedGradientBackground(modifier = modifier) {
    Column(
      modifier = Modifier.align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      AppLauncherIcon(modifier = Modifier.size(ICON_SIZE))
      Spacer(Modifier.height(36.dp))
      AnimatedVisibility(
        visible = nameVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
      ) {
        Text(
          text = stringResource(R.string.app_title),
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
      }
    }
  }
}

private val ICON_SIZE = 120.dp
private const val NAME_APPEARANCE_DELAY_MILLIS = 350L
