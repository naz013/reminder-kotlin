package com.elementary.tasks.settings.troubleshooting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem

@Composable
fun TroubleshootingScreen(
  state: TroubleshootingScreenState,
  onSendLogsClick: () -> Unit,
  onDisableOptimizationClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()),
  ) {
    if (state.showSendLogs) {
      SettingsItem(
        title = stringResource(R.string.send_logs),
        icon = painterResource(R.drawable.ic_fluent_send_logging),
        dividerBottom = true,
        onClick = onSendLogsClick,
      )
    }

    if (state.showBatteryOptimizationCard) {
      Card(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(16.dp),
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          Row {
            Icon(
              painter = painterResource(R.drawable.ic_fluent_battery_saver),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurface,
              modifier =
                Modifier
                  .size(24.dp)
                  .padding(top = 4.dp),
            )
            Text(
              text = stringResource(R.string.troubleshooting_battery_optimization_text),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(start = 8.dp),
            )
          }
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End,
          ) {
            Button(onClick = onDisableOptimizationClick) {
              Text(stringResource(R.string.troubleshooting_disable_battery_optimization))
            }
          }
        }
      }
    }

    if (state.showEmptyView) {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty_status))
        LottieAnimation(
          composition = composition,
          iterations = LottieConstants.IterateForever,
          modifier = Modifier.size(200.dp),
        )
        Text(
          text = stringResource(R.string.troubleshooting_no_issues_found),
          style = MaterialTheme.typography.headlineSmall,
          modifier = Modifier.padding(horizontal = 24.dp),
        )
      }
    }
  }
}
