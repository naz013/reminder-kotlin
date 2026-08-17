package com.github.naz013.feature.reminder.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R

@Composable
internal fun ReminderFullscreenMapScreen(
  isLoading: Boolean,
  onMoveToPlaceClick: () -> Unit,
  mapContent: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize()) {
    if (isLoading) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    } else {
      mapContent()
      ExtendedFloatingActionButton(
        onClick = onMoveToPlaceClick,
        icon = { Icon(painterResource(R.drawable.ic_fluent_place), contentDescription = null) },
        text = { Text(stringResource(R.string.move_to_place)) },
        modifier =
          Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
      )
    }
  }
}
