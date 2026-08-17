package com.github.naz013.feature.settings.location

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem
import com.github.naz013.ui.common.compose.foundation.dialog.SingleChoiceDialog

@Composable
fun LocationSettingsScreen(
  state: LocationSettingsState,
  onNotificationToggle: () -> Unit,
  onRadiusClick: () -> Unit,
  onRadiusPreviewChange: (Int) -> Unit,
  onRadiusConfirm: () -> Unit,
  onMapTypeClick: () -> Unit,
  onMapTypeOptionSelected: (Int) -> Unit,
  onMapStyleClick: () -> Unit,
  onMarkerStyleClick: () -> Unit,
  onTrackerClick: () -> Unit,
  onTrackerPreviewChange: (Int) -> Unit,
  onTrackerConfirm: () -> Unit,
  onPlacesClick: () -> Unit,
  onDialogDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()),
  ) {
    SettingsSwitchItem(
      title = stringResource(R.string.distance_notification),
      checked = state.isNotificationChecked,
      onCheckedChange = { onNotificationToggle() },
      subtitleOn = stringResource(R.string.show_notification_about_left_distance),
      subtitleOff = stringResource(R.string.do_not_show_notification),
      icon = painterResource(R.drawable.ic_fluent_alert),
      dividerBottom = true,
    )
    SettingsItem(
      title = stringResource(R.string.radius),
      subtitle = state.radiusText,
      icon = painterResource(R.drawable.ic_builder_map_radius),
      dividerBottom = true,
      onClick = onRadiusClick,
    )
    SettingsItem(
      title = stringResource(R.string.map_type),
      subtitle = state.mapTypeName,
      icon = painterResource(R.drawable.ic_fluent_map),
      dividerBottom = true,
      onClick = onMapTypeClick,
    )
    SettingsItem(
      title = stringResource(R.string.map_style),
      subtitle = state.mapStyleName,
      icon = painterResource(R.drawable.ic_fluent_style_guide),
      enabled = state.isMapStyleRowEnabled,
      dividerBottom = true,
      onClick = onMapStyleClick,
      trailing =
        if (state.mapStylePreviewRes != 0) {
          {
            Image(
              painter = painterResource(state.mapStylePreviewRes),
              contentDescription = null,
              modifier = Modifier.size(40.dp),
            )
          }
        } else {
          null
        },
    )
    if (state.isMarkerStyleVisible) {
      SettingsItem(
        title = stringResource(R.string.style_of_marker),
        icon = painterResource(R.drawable.ic_fluent_color),
        dividerBottom = true,
        onClick = onMarkerStyleClick,
        trailing = {
          Icon(
            painter = painterResource(R.drawable.ic_fluent_place),
            contentDescription = null,
            tint = Color(state.markerColor),
          )
        },
      )
    }
    SettingsItem(
      title = stringResource(R.string.tracking_settings),
      icon = painterResource(R.drawable.ic_fluent_location_live),
      dividerBottom = true,
      onClick = onTrackerClick,
    )
    if (state.hasLocation) {
      SettingsItem(
        title = stringResource(R.string.places),
        icon = painterResource(R.drawable.ic_fluent_place),
        dividerBottom = true,
        onClick = onPlacesClick,
      )
    }
  }

  when (val dialog = state.dialog) {
    is LocationSettingsDialog.MapType -> {
      SingleChoiceDialog(
        title = stringResource(R.string.map_type),
        options = dialog.options,
        selectedIndex = dialog.selectedIndex,
        onOptionSelected = onMapTypeOptionSelected,
        onDismiss = onDialogDismiss,
      )
    }

    is LocationSettingsDialog.Radius -> {
      AlertDialog(
        onDismissRequest = onDialogDismiss,
        title = { Text(stringResource(R.string.radius)) },
        text = {
          Column {
            Text(text = dialog.formattedValue, style = MaterialTheme.typography.bodyLarge)
            Slider(
              value = dialog.value.toFloat(),
              onValueChange = { onRadiusPreviewChange(it.toInt()) },
              valueRange = 0f..dialog.valueTo,
              modifier = Modifier.fillMaxWidth(),
            )
          }
        },
        confirmButton = { TextButton(onClick = onRadiusConfirm) { Text(stringResource(R.string.ok)) } },
        dismissButton = { TextButton(onClick = onDialogDismiss) { Text(stringResource(R.string.cancel)) } },
      )
    }

    is LocationSettingsDialog.Tracker -> {
      AlertDialog(
        onDismissRequest = onDialogDismiss,
        title = { Text(stringResource(R.string.tracking_settings)) },
        text = {
          Column {
            Text(
              text = stringResource(R.string.for_lower_battery_usage_set_bigger_values),
              style = MaterialTheme.typography.titleSmall,
            )
            Text(
              text = stringResource(R.string.x_seconds, dialog.seconds.toString()),
              style = MaterialTheme.typography.titleLarge,
            )
            Slider(
              value = dialog.seconds.toFloat(),
              onValueChange = { onTrackerPreviewChange(it.toInt()) },
              valueRange = 1f..30f,
              steps = 28,
              modifier = Modifier.fillMaxWidth(),
            )
          }
        },
        confirmButton = { TextButton(onClick = onTrackerConfirm) { Text(stringResource(R.string.ok)) } },
        dismissButton = { TextButton(onClick = onDialogDismiss) { Text(stringResource(R.string.cancel)) } },
      )
    }

    null -> Unit
  }
}
