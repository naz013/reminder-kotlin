package com.github.naz013.feature.settings.export.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.TooltipIconButton
import com.github.naz013.ui.common.compose.foundation.component.AnimatedGradientBackground
import com.github.naz013.ui.common.compose.withAlpha

@Composable
internal fun CloudServicesScreen(
  state: CloudServicesState,
  onBackClick: () -> Unit,
  onDropboxClick: () -> Unit,
  onGoogleDriveClick: () -> Unit,
  onGoogleTasksClick: () -> Unit,
) {
  AnimatedGradientBackground {
    Column(modifier = Modifier.fillMaxSize()) {
      Row(
        modifier =
          Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Spacer(modifier = Modifier.width(16.dp))
        TooltipIconButton(contentDescription = stringResource(R.string.cd_back)) {
          IconButton(
            onClick = onBackClick,
            modifier =
              Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background.withAlpha(0.25f)),
          ) {
            Icon(
              painter = AppIcons.Builder.ArrowLeft,
              contentDescription = stringResource(R.string.cd_back),
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
      ) {
        Spacer(modifier = Modifier.height(24.dp))

        if (state.isDropboxVisible || state.isGoogleDriveVisible) {
          SectionTitle(text = stringResource(R.string.application_data))
          Spacer(modifier = Modifier.height(16.dp))
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              if (state.isDropboxVisible) {
                ServiceRow(
                  title = stringResource(R.string.dropbox),
                  isLoggedIn = state.isDropboxLoggedIn,
                  enabled = !state.isLoading,
                  onClick = onDropboxClick,
                )
              }
              if (state.isGoogleDriveVisible) {
                ServiceRow(
                  title = stringResource(R.string.google_drive),
                  isLoggedIn = state.isGoogleDriveLoggedIn,
                  enabled = !state.isLoading,
                  onClick = onGoogleDriveClick,
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(32.dp))
        }

        if (state.isGoogleTasksVisible) {
          SectionTitle(text = stringResource(R.string.google_tasks))
          Spacer(modifier = Modifier.height(16.dp))
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(20.dp)) {
              ServiceRow(
                title = stringResource(R.string.google_tasks),
                isLoggedIn = state.isGoogleTasksLoggedIn,
                enabled = !state.isLoading,
                onClick = onGoogleTasksClick,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun SectionTitle(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.tertiary,
  )
}

@Composable
private fun ServiceRow(
  title: String,
  isLoggedIn: Boolean,
  enabled: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
    )
    FilledTonalButton(onClick = onClick, enabled = enabled) {
      Text(text = stringResource(if (isLoggedIn) R.string.logout else R.string.log_in))
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun CloudServicesScreenPreview() {
  CloudServicesScreen(
    state = CloudServicesState(),
    onBackClick = {},
    onDropboxClick = {},
    onGoogleDriveClick = {},
    onGoogleTasksClick = {},
  )
}
