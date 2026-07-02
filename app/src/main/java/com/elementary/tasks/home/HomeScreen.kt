package com.elementary.tasks.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R

@Composable
fun HomeScreen(
  modifier: Modifier = Modifier,
  bannerState: BannerState?,
  onPrivacyPolicyClick: () -> Unit = {},
  onPrivacyAcceptClick: () -> Unit = {},
  onLoginDismissClick: () -> Unit = {},
  onLoginClick: () -> Unit = {},
  onWhatsNewDetailsClick: () -> Unit = {},
  onWhatsNewDismissClick: () -> Unit = {},
  content: @Composable BoxScope.() -> Unit,
) {
  Box(modifier = modifier) {
    content()
    when (bannerState) {
      is BannerState.Privacy -> PrivacyBanner(
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        onAcceptClick = onPrivacyAcceptClick,
      )
      is BannerState.Login -> LoginBanner(
        onDismissClick = onLoginDismissClick,
        onLoginClick = onLoginClick,
      )
      is BannerState.WhatsNew -> WhatsNewBanner(
        onDetailsClick = onWhatsNewDetailsClick,
        onDismissClick = onWhatsNewDismissClick,
      )
      null -> {}
    }
  }
}

@Composable
private fun BoxScope.PrivacyBanner(
  modifier: Modifier = Modifier,
  onPrivacyPolicyClick: () -> Unit,
  onAcceptClick: () -> Unit,
) {
  HomeBanner(
    modifier = modifier,
    text = stringResource(R.string.by_continue_using_reminder_application_you_accept_privacy_policy),
    negativeText = stringResource(R.string.privacy_policy),
    positiveText = stringResource(R.string.accept),
    onNegativeClick = { onPrivacyPolicyClick() },
    onPositiveClick = onAcceptClick,
  )
}

@Composable
private fun BoxScope.LoginBanner(
  modifier: Modifier = Modifier,
  onDismissClick: () -> Unit,
  onLoginClick: () -> Unit,
) {
  HomeBanner(
    modifier = modifier,
    text = stringResource(R.string.if_you_have_backed_up_data_you_can_log_in_to_your_cloud_drive_to_restore_it),
    negativeText = stringResource(R.string.dismiss),
    positiveText = stringResource(R.string.log_in),
    onNegativeClick = onDismissClick,
    onPositiveClick = onLoginClick,
  )
}

@Composable
private fun BoxScope.WhatsNewBanner(
  modifier: Modifier = Modifier,
  onDetailsClick: () -> Unit,
  onDismissClick: () -> Unit,
) {
  HomeBanner(
    modifier = modifier,
    text = stringResource(R.string.whats_new_banner_text),
    negativeText = stringResource(R.string.ok),
    positiveText = stringResource(R.string.whats_new_details),
    onNegativeClick = onDismissClick,
    onPositiveClick = onDetailsClick,
  )
}

@Composable
private fun BoxScope.HomeBanner(
  modifier: Modifier = Modifier,
  text: String,
  negativeText: String,
  positiveText: String,
  onNegativeClick: () -> Unit,
  onPositiveClick: () -> Unit,
) {
  ElevatedCard(
    modifier = modifier
      .align(Alignment.BottomCenter)
      .padding(16.dp)
      .fillMaxWidth(),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
    ),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
      ) {
        TextButton(onClick = onNegativeClick) {
          Text(text = negativeText, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        TextButton(onClick = onPositiveClick) {
          Text(text = positiveText, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
      }
    }
  }
}

// Previews

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun PrivacyBannerPreview() {
  Box(modifier = Modifier.fillMaxSize()) {
    PrivacyBanner(
      onPrivacyPolicyClick = {},
      onAcceptClick = {},
    )
  }
}

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun LoginBannerPreview() {
  Box(modifier = Modifier.fillMaxSize()) {
    LoginBanner(
      onDismissClick = {},
      onLoginClick = {},
    )
  }
}

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun WhatsNewBannerPreview() {
  Box(modifier = Modifier.fillMaxSize()) {
    WhatsNewBanner(
      onDetailsClick = {},
      onDismissClick = {},
    )
  }
}
