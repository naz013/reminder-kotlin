package com.github.naz013.feature.settings.protrial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.AnimatedGradientBackground
import com.github.naz013.ui.common.compose.foundation.component.GradientHeroCard
import com.github.naz013.ui.common.compose.foundation.component.GradientScreenHeader

@Composable
internal fun ProTrialScreen(
  modifier: Modifier = Modifier,
  state: ProTrialState,
  onBackClick: () -> Unit,
  onActivateClick: () -> Unit,
  onExtendClick: () -> Unit,
  renderAsDetailPane: Boolean = false,
) {
  AnimatedGradientBackground(modifier = modifier) {
    Column(modifier = Modifier.fillMaxSize()) {
      GradientScreenHeader(
        onBackClick = onBackClick,
        contentDescription = stringResource(if (renderAsDetailPane) R.string.acc_close else R.string.cd_back),
        icon = if (renderAsDetailPane) AppIcons.Fluent.Dismiss else AppIcons.Builder.ArrowLeft,
      )
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp),
      ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
          text = stringResource(R.string.pro_trial_title),
          style = MaterialTheme.typography.headlineSmallEmphasized,
          color = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        GradientHeroCard(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = stringResource(R.string.pro_trial_how_it_works_title),
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = stringResource(R.string.pro_trial_how_it_works),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Spacer(modifier = Modifier.height(24.dp))
        ProTrialStatus(state = state)
        Button(
          onClick = onActivateClick,
          enabled = !state.isRunning && !state.isTrialActive,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(text = stringResource(R.string.pro_trial_activate))
        }
        if (state.isTrialActive) {
          Spacer(modifier = Modifier.height(8.dp))
          FilledTonalButton(
            onClick = onExtendClick,
            enabled = state.canExtend && !state.isRunning,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(text = stringResource(R.string.pro_trial_extend))
          }
          if (!state.canExtend) {
            Text(
              text = stringResource(R.string.pro_trial_limit_reached),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(top = 8.dp),
            )
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun ProTrialStatus(
  modifier: Modifier = Modifier,
  state: ProTrialState,
) {
  val hasStatus = state.isTrialActive || state.isProgressVisible || state.message != null
  if (!hasStatus) return

  Column(modifier = modifier.padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    if (state.isTrialActive) {
      Text(
        text = stringResource(R.string.pro_trial_active_title),
        style = MaterialTheme.typography.titleMediumEmphasized,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = stringResource(R.string.pro_trial_remaining, state.remainingTime),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
    if (state.isProgressVisible) {
      Text(
        text = stringResource(R.string.pro_trial_ads_progress, state.adsWatched, state.requiredAds),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
    state.message?.let { message ->
      Text(
        text = stringResource(message.textRes()),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.error,
      )
    }
  }
}

private fun ProTrialMessage.textRes(): Int {
  return when (this) {
    ProTrialMessage.ADS_UNAVAILABLE -> R.string.pro_trial_ads_unavailable
    ProTrialMessage.CANCELLED -> R.string.pro_trial_cancelled
    ProTrialMessage.LIMIT_REACHED -> R.string.pro_trial_limit_reached
  }
}

@Preview(showBackground = true)
@Composable
private fun ProTrialScreenPreview() {
  AppTheme(darkTheme = false) {
    ProTrialScreen(
      state = ProTrialState(isRunning = true, requiredAds = 1),
      onBackClick = {},
      onActivateClick = {},
      onExtendClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun ProTrialScreenPreview_Active() {
  AppTheme(darkTheme = true) {
    ProTrialScreen(
      state = ProTrialState(isTrialActive = true, canExtend = true, remainingTime = "23:40", requiredAds = 1),
      onBackClick = {},
      onActivateClick = {},
      onExtendClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun ProTrialScreenPreview_LimitReached() {
  AppTheme(darkTheme = false) {
    ProTrialScreen(
      state = ProTrialState(isTrialActive = true, remainingTime = "119:40", requiredAds = 1),
      onBackClick = {},
      onActivateClick = {},
      onExtendClick = {},
    )
  }
}
