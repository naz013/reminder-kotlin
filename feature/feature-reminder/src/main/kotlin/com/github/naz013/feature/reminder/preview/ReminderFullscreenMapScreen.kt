package com.github.naz013.feature.reminder.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons

@Composable
internal fun ReminderFullscreenMapScreen(
  modifier: Modifier = Modifier,
  isLoading: Boolean,
  onMoveToPlaceClick: () -> Unit,
  mapContent: @Composable () -> Unit,
) {
  Box(modifier = modifier.fillMaxSize()) {
    if (isLoading) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    } else {
      mapContent()
      SmallExtendedFloatingActionButton(
        onClick = onMoveToPlaceClick,
        icon = { Icon(AppIcons.Fluent.Place, contentDescription = null) },
        text = { Text(stringResource(R.string.move_to_place)) },
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
          .padding(bottom = 16.dp),
      )
    }
  }
}
