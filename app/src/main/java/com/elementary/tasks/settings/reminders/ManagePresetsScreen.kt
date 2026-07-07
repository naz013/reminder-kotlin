package com.elementary.tasks.settings.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.reminder.build.preset.PresetListItem
import com.github.naz013.ui.common.compose.AppTheme

@Composable
fun ManagePresetsScreen(
  presets: List<UiPresetList>,
  onDeleteClick: (UiPresetList) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
  ) {
    if (presets.isEmpty()) {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
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
          text = stringResource(R.string.recur_no_presets),
          style = MaterialTheme.typography.headlineSmall,
          modifier = Modifier.padding(horizontal = 24.dp),
        )
      }
    } else {
      LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(presets, key = { it.id }) { preset ->
          PresetListItem(
            preset = preset,
            onClick = {},
            onDeleteClick = { onDeleteClick(preset) },
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun ManagePresetsScreenPreview() {
  AppTheme {
    ManagePresetsScreen(
      presets =
        listOf(
          UiPresetList(name = "Daily standup", id = "1", description = "RRULE:FREQ=DAILY"),
          UiPresetList(name = "Weekly review", id = "2", description = "RRULE:FREQ=WEEKLY"),
        ),
      onDeleteClick = {},
    )
  }
}
