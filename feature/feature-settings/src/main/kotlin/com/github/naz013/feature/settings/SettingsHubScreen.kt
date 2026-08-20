package com.github.naz013.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem

private val BannerHorizontalPadding = 16.dp
private val BannerVerticalPadding = 8.dp

@Composable
internal fun SettingsHubScreen(
  modifier: Modifier = Modifier,
  state: SettingsHubState,
  onBuyProClick: () -> Unit,
  onUpdateClick: () -> Unit,
  onGeneralClick: () -> Unit,
  onBackupClick: () -> Unit,
  onCalendarClick: () -> Unit,
  onRemindersClick: () -> Unit,
  onBirthdaysClick: () -> Unit,
  onSecurityClick: () -> Unit,
  onNotesClick: () -> Unit,
  onOtherClick: () -> Unit,
  onDeveloperClick: () -> Unit,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState())
        .padding(vertical = 8.dp),
  ) {
    if (state.isBuyProBadgeVisible) {
      SettingsBanner(
        text = stringResource(R.string.pro_version),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        icon = AppIcons.Fluent.Star,
        trailingIcon = AppIcons.Fluent.ChevronRight,
        emphasized = true,
        onClick = onBuyProClick,
      )
    }
    if (state.saleMessage != null) {
      SettingsBanner(
        text = state.saleMessage,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
      )
    }
    if (state.updateMessage != null) {
      SettingsBanner(
        text = state.updateMessage,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        trailingIcon = AppIcons.Fluent.ChevronRight,
        onClick = onUpdateClick,
      )
    }
    if (state.internalMessage != null) {
      SettingsBanner(
        text = state.internalMessage,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
      )
    }
    if (state.isPlayServicesWarningVisible) {
      SettingsBanner(
        text = stringResource(R.string.google_play_services_not_found_some_functionality_is_disabled),
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        icon = AppIcons.Fluent.Warning,
      )
    }
    if (state.isDoNotDisturbActive) {
      SettingsBanner(
        text = stringResource(R.string.do_not_disturb),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        icon = painterResource(R.drawable.ic_moon),
      )
    }

    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = BannerHorizontalPadding, vertical = BannerVerticalPadding),
    ) {
      SettingsItem(
        title = stringResource(R.string.general),
        icon = painterResource(R.drawable.ic_fluent_system),
        dividerBottom = true,
        onClick = onGeneralClick,
      )
      SettingsItem(
        title = stringResource(R.string.backup),
        icon = AppIcons.Fluent.CloudSyncComplete,
        dividerBottom = true,
        onClick = onBackupClick,
      )
      SettingsItem(
        title = stringResource(R.string.calendar),
        icon = painterResource(R.drawable.ic_builder_by_monthday),
        dividerBottom = true,
        onClick = onCalendarClick,
      )
      SettingsItem(
        title = stringResource(R.string.reminders_),
        icon = painterResource(R.drawable.ic_fluent_clock_alarm),
        dividerBottom = true,
        onClick = onRemindersClick,
      )
      SettingsItem(
        title = stringResource(R.string.birthdays),
        icon = painterResource(R.drawable.ic_fluent_food_cake),
        dividerBottom = true,
        onClick = onBirthdaysClick,
      )
      SettingsItem(
        title = stringResource(R.string.security),
        icon = painterResource(R.drawable.ic_fluent_lock),
        dividerBottom = true,
        onClick = onSecurityClick,
      )
      SettingsItem(
        title = stringResource(R.string.notes),
        icon = painterResource(R.drawable.ic_fluent_note),
        dividerBottom = true,
        onClick = onNotesClick,
      )
      SettingsItem(
        title = stringResource(R.string.other),
        icon = painterResource(R.drawable.ic_fluent_launcher_settings),
        dividerBottom = state.isDeveloperOptionVisible,
        onClick = onOtherClick,
      )
      if (state.isDeveloperOptionVisible) {
        SettingsItem(
          title = "Developer",
          dividerBottom = false,
          onClick = onDeveloperClick,
        )
      }
    }
  }
}

/**
 * A tonal, full-width status card used for every "floating message" on the settings hub (Buy Pro
 * upsell, sale/update/internal messages, the Play Services warning, and the Do Not Disturb
 * indicator) so they read as distinct, glanceable banners instead of plain text sitting on the
 * screen background.
 */
@Composable
private fun SettingsBanner(
  text: String,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
  icon: Painter? = null,
  trailingIcon: Painter? = null,
  emphasized: Boolean = false,
  onClick: (() -> Unit)? = null,
) {
  val colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)
  val cardModifier =
    modifier
      .fillMaxWidth()
      .padding(horizontal = BannerHorizontalPadding, vertical = BannerVerticalPadding)
  val content: @Composable () -> Unit = {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (icon != null) {
        Icon(
          painter = icon,
          contentDescription = null,
          tint = contentColor,
          modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
      }
      Text(
        text = text,
        style = if (emphasized) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall,
        color = contentColor,
        modifier = Modifier.weight(1f),
      )
      if (trailingIcon != null) {
        Spacer(modifier = Modifier.width(16.dp))
        Icon(painter = trailingIcon, contentDescription = null, tint = contentColor)
      }
    }
  }

  if (onClick != null) {
    Card(onClick = onClick, colors = colors, modifier = cardModifier, content = { content() })
  } else {
    Card(colors = colors, modifier = cardModifier, content = { content() })
  }
}
