package com.github.naz013.feature.settings.location

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem

@Composable
internal fun MapStyleScreen(
  state: MapStyleState,
  onOptionSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()),
  ) {
    state.options.forEachIndexed { index, option ->
      val isSelected = option.index == state.selectedIndex
      SettingsItem(
        title = stringResource(option.titleRes),
        dividerTop = index == 0,
        dividerBottom = true,
        onClick = { onOptionSelected(option.index) },
        trailing = {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            RadioButton(selected = isSelected, onClick = null)
            option.previews.forEach { preview ->
              Image(
                painter = painterResource(preview),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
              )
            }
          }
        },
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun MapStyleScreenPreview() {
  AppTheme {
    MapStyleScreen(
      state =
        MapStyleState(
          options =
            listOf(
              MapStyleOption(
                index = 6,
                titleRes = R.string.auto,
                previews =
                  listOf(
                    R.drawable.preview_map_day,
                    R.drawable.preview_map_night,
                  ),
              ),
              MapStyleOption(
                index = 0,
                titleRes = R.string.day,
                previews = listOf(R.drawable.preview_map_day),
              ),
            ),
          selectedIndex = 0,
        ),
      onOptionSelected = {},
    )
  }
}
