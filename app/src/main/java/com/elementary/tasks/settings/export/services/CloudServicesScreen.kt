package com.elementary.tasks.settings.export.services

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
import androidx.compose.material3.CircularProgressIndicator
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
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.AnimatedGradientBackground
import com.github.naz013.ui.common.compose.withAlpha
import com.github.naz013.ui.common.R as UiR

@Composable
fun CloudServicesScreen(
  isLoading: Boolean,
  isDropboxVisible: Boolean,
  isDropboxLoggedIn: Boolean,
  isGoogleDriveVisible: Boolean,
  isGoogleDriveLoggedIn: Boolean,
  isGoogleTasksVisible: Boolean,
  isGoogleTasksLoggedIn: Boolean,
  onBackClick: () -> Unit,
  onDropboxClick: () -> Unit,
  onGoogleDriveClick: () -> Unit,
  onGoogleTasksClick: () -> Unit,
) {
  AnimatedGradientBackground {
    Column(modifier = Modifier.fillMaxSize()) {
      Row(
        modifier = Modifier
          .statusBarsPadding()
          .fillMaxWidth()
          .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(
          onClick = onBackClick,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background.withAlpha(0.25f)),
        ) {
          Icon(
            painter = painterResource(UiR.drawable.ic_builder_arrow_left),
            contentDescription = stringResource(UiR.string.cd_back),
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp),
      ) {
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp),
          ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
              text = stringResource(R.string.please_wait),
              style = MaterialTheme.typography.titleMedium,
            )
          }
        }

        if (isDropboxVisible || isGoogleDriveVisible) {
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
              if (isDropboxVisible) {
                ServiceRow(
                  title = stringResource(R.string.dropbox),
                  isLoggedIn = isDropboxLoggedIn,
                  enabled = !isLoading,
                  onClick = onDropboxClick,
                )
              }
              if (isGoogleDriveVisible) {
                ServiceRow(
                  title = stringResource(R.string.google_drive),
                  isLoggedIn = isGoogleDriveLoggedIn,
                  enabled = !isLoading,
                  onClick = onGoogleDriveClick,
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(32.dp))
        }

        if (isGoogleTasksVisible) {
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
                isLoggedIn = isGoogleTasksLoggedIn,
                enabled = !isLoading,
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
    isLoading = false,
    isDropboxVisible = true,
    isDropboxLoggedIn = false,
    isGoogleDriveVisible = true,
    isGoogleDriveLoggedIn = true,
    isGoogleTasksVisible = true,
    isGoogleTasksLoggedIn = false,
    onBackClick = {},
    onDropboxClick = {},
    onGoogleDriveClick = {},
    onGoogleTasksClick = {},
  )
}
