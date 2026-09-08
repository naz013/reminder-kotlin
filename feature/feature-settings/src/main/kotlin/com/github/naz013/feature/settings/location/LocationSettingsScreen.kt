package com.github.naz013.feature.settings.location

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSearchItemKeys
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem
import com.github.naz013.ui.common.compose.foundation.dialog.SeekValueDialog
import com.github.naz013.ui.common.compose.foundation.dialog.SingleChoiceDialog

@Composable
internal fun LocationSettingsScreen(
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
    modifier = modifier
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
      itemKey = SettingsSearchItemKeys.LOCATION_NOTIFICATION_TOGGLE,
      dividerBottom = true,
    )
    SettingsItem(
      title = stringResource(R.string.radius),
      subtitle = state.radiusText,
      icon = painterResource(R.drawable.ic_builder_map_radius),
      itemKey = SettingsSearchItemKeys.LOCATION_RADIUS,
      dividerBottom = true,
      onClick = onRadiusClick,
    )
    SettingsItem(
      title = stringResource(R.string.map_type),
      subtitle = state.mapTypeName,
      icon = painterResource(R.drawable.ic_fluent_map),
      itemKey = SettingsSearchItemKeys.LOCATION_MAP_TYPE,
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
      trailing = if (state.mapStylePreviewRes != 0) {
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
        itemKey = SettingsSearchItemKeys.LOCATION_MARKER_STYLE,
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
      itemKey = SettingsSearchItemKeys.LOCATION_TRACKING,
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
      SeekValueDialog(
        title = stringResource(R.string.radius),
        value = dialog.value,
        valueText = dialog.formattedValue,
        valueRange = 0f..dialog.valueTo,
        onValueChange = onRadiusPreviewChange,
        onConfirm = onRadiusConfirm,
        onDismiss = onDialogDismiss,
      )
    }

    is LocationSettingsDialog.Tracker -> {
      SeekValueDialog(
        title = stringResource(R.string.tracking_settings),
        value = dialog.seconds,
        valueText = stringResource(R.string.x_seconds, dialog.seconds.toString()),
        valueRange = 1f..30f,
        steps = 28,
        description = stringResource(R.string.for_lower_battery_usage_set_bigger_values),
        valueTextStyle = MaterialTheme.typography.titleLarge,
        onValueChange = onTrackerPreviewChange,
        onConfirm = onTrackerConfirm,
        onDismiss = onDialogDismiss,
      )
    }

    null -> Unit
  }
}
