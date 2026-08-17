package com.github.naz013.group.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSectionHeader
import com.github.naz013.ui.common.compose.toColor
import com.github.naz013.ui.common.text.UiTextElement
import com.github.naz013.ui.common.text.UiTextFormat
import com.github.naz013.ui.notification.settings.NotificationOverrideSubtitles
import com.github.naz013.ui.reminder.UiReminderList
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListState

private val COLOR_DOT_SIZE = 14.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GroupDetailsScreen(
  modifier: Modifier = Modifier,
  state: GroupDetailsState,
  onBackClick: () -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onReminderClick: (String) -> Unit,
  onAddClick: () -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            GroupColorDot(color = state.color)
            Text(
              text = state.title,
              modifier = Modifier.padding(start = 12.dp),
            )
          }
        },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        actions = {
          MenuIconButton(
            icon = AppIcons.Fluent.Add,
            contentDescription = stringResource(R.string.acc_add_reminder),
            onClick = onAddClick,
            iconColor = MaterialTheme.colorScheme.primary,
          )
          OverflowMenu(
            canDelete = state.canDelete,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
  ) { padding ->
    if (state.isLoading) {
      Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
      return@Scaffold
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
      item { NotificationOverridesSection(state.notificationSubtitles) }

      item { adsContent() }

      item { SectionHeader(text = stringResource(R.string.reminders)) }
      if (state.reminders.isEmpty()) {
        item { RemindersEmptyState() }
      } else {
        items(state.reminders, key = { it.id }) { reminder ->
          GroupReminderRow(
            item = reminder,
            onClick = { onReminderClick(reminder.id) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun GroupColorDot(color: Int) {
  Box(
    modifier =
      Modifier
        .size(COLOR_DOT_SIZE)
        .clip(CircleShape)
        .background(color.toColor()),
  )
}

@Composable
private fun OverflowMenu(
  canDelete: Boolean,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val items =
    buildList {
      add(PopupMenuItem(id = OverflowAction.EDIT.ordinal, title = stringResource(R.string.edit), iconRes = R.drawable.ic_fluent_edit))
      if (canDelete) {
        add(
          PopupMenuItem(
            id = OverflowAction.DELETE.ordinal,
            title = stringResource(R.string.delete),
            iconRes = R.drawable.ic_fluent_delete,
          ),
        )
      }
    }
  Box {
    MenuIconButton(
      icon = painterResource(R.drawable.ic_fluent_more_vertical),
      contentDescription = stringResource(R.string.more_options),
      onClick = { expanded = true },
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items = items,
      onItemClick = { id ->
        expanded = false
        when (OverflowAction.entries[id]) {
          OverflowAction.EDIT -> onEditClick()
          OverflowAction.DELETE -> onDeleteClick()
        }
      },
    )
  }
}

private enum class OverflowAction { EDIT, DELETE }

@Composable
private fun SectionHeader(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
  )
}

@Composable
private fun NotificationOverridesSection(subtitles: NotificationOverrideSubtitles) {
  if (subtitles.allDefault) return

  SettingsSectionHeader(stringResource(R.string.notification_overrides))
  subtitles.priority?.let {
    SettingsItem(
      title = stringResource(R.string.reminder_default_priority),
      subtitle = subtitles.priority,
      dividerBottom = true,
      icon = AppIcons.Fluent.Star,
    )
  }
  subtitles.repeatNotification?.let {
    SettingsItem(
      title = stringResource(R.string.repeat_notification),
      subtitle = subtitles.repeatNotification,
      dividerBottom = true,
    )
  }
  subtitles.delayMinutes?.let {
    SettingsItem(
      title = stringResource(R.string.notification_delay),
      subtitle = subtitles.delayMinutes,
      dividerBottom = true,
      icon = AppIcons.Builder.Interval,
    )
  }
  subtitles.category?.let {
    SettingsItem(
      title = stringResource(R.string.notification_category),
      subtitle = subtitles.category,
      dividerBottom = true,
      icon = AppIcons.Fluent.ChannelNotifications,
    )
  }
  subtitles.vibrate?.let {
    SettingsItem(
      title = stringResource(R.string.default_vibrate),
      subtitle = subtitles.vibrate,
      dividerBottom = true,
      icon = AppIcons.Fluent.PhoneVibrate,
    )
  }
  subtitles.vibrationPattern?.let {
    SettingsItem(
      title = stringResource(R.string.vibration_pattern),
      subtitle = subtitles.vibrationPattern,
      dividerBottom = true,
      icon = AppIcons.Fluent.PhoneVibrate,
    )
  }
  subtitles.bypassDnd?.let {
    SettingsItem(
      title = stringResource(R.string.bypass_do_not_disturb),
      subtitle = subtitles.bypassDnd,
      dividerBottom = true,
      icon = AppIcons.Fluent.Sleep,
    )
  }
  subtitles.wakeScreen?.let {
    SettingsItem(
      title = stringResource(R.string.wake_screen),
      subtitle = subtitles.wakeScreen,
      dividerBottom = true,
    )
  }
  subtitles.lockScreenVisibility?.let {
    SettingsItem(
      title = stringResource(R.string.lock_screen_visibility),
      subtitle = subtitles.lockScreenVisibility,
      dividerBottom = true,
      icon = AppIcons.Fluent.LockShield,
    )
  }
}

@Composable
private fun RemindersEmptyState() {
  Text(
    text = stringResource(R.string.group_has_no_active_reminders),
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
  )
}

@Preview(showBackground = true)
@Composable
private fun GroupDetailsScreenPreview() {
  AppTheme {
    GroupDetailsScreen(
      state =
        GroupDetailsState(
          isLoading = false,
          title = "Work",
          color = 0xFF2196F3.toInt(),
          canDelete = true,
          notificationSubtitles =
            NotificationOverrideSubtitles(
              priority = "High",
              repeatNotification = "Inherited: On",
              delayMinutes = "Inherited: 0 minutes",
              category = "Inherited: Reminder",
              vibrate = "Inherited: On",
              vibrationPattern = "Inherited: Default",
              bypassDnd = "Off",
              wakeScreen = "Inherited: Off",
              lockScreenVisibility = "Inherited: Hide sensitive content",
            ),
          reminders =
            listOf(
              UiReminderList(
                id = "1",
                noteId = null,
                dueDateTime = null,
                mainText = UiTextElement(text = "Buy milk", textFormat = UiTextFormat(fontSize = 16f)),
                secondaryText = UiTextElement(text = "Today, 18:00", textFormat = UiTextFormat(fontSize = 14f)),
                tertiaryText = null,
                tags = emptyList(),
                actions = UiReminderListActions(),
                state = UiReminderListState(isActive = true),
              ),
            ),
        ),
      onBackClick = {},
      onEditClick = {},
      onDeleteClick = {},
      onReminderClick = {},
      adsContent = {},
      onAddClick = {},
    )
  }
}
