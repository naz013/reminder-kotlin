package com.elementary.tasks.reminder.build

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderConfigureScreen(
  state: BuilderConfigureState,
  onBackClick: () -> Unit,
  onSummaryToggle: () -> Unit,
  onBeforeToggle: () -> Unit,
  onRepeatToggle: () -> Unit,
  onRepeatLimitToggle: () -> Unit,
  onPriorityToggle: () -> Unit,
  onAttachmentToggle: () -> Unit,
  onCalendarToggle: () -> Unit,
  onTasksToggle: () -> Unit,
  onExtraToggle: () -> Unit,
  onLedToggle: () -> Unit,
  onICalendarToggle: () -> Unit,
  onMakeCallToggle: () -> Unit,
  onSendSmsToggle: () -> Unit,
  onOpenAppToggle: () -> Unit,
  onOpenLinkToggle: () -> Unit,
  onSendEmailToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.configure)) },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(painter = painterResource(R.drawable.ic_builder_arrow_left), contentDescription = null)
          }
        },
        colors = TopAppbarColor,
      )
    },
  ) { padding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(padding)
          .background(MaterialTheme.colorScheme.background)
          .verticalScroll(rememberScrollState()),
    ) {
      Text(
        text = stringResource(R.string.enable_or_disable_some_parameters_on_reminder_creation_screen),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
      )

      SettingsSwitchItem(
        title = stringResource(R.string.summary_field),
        checked = state.isSummaryChecked,
        onCheckedChange = { onSummaryToggle() },
        subtitleOn = stringResource(R.string.automatically_add_summary_field_when_create_the_reminder),
        subtitleOff = stringResource(R.string.do_not_add_summary_field_when_create_the_reminder),
        icon = painterResource(R.drawable.ic_fluent_text),
        dividerBottom = true,
      )
      SettingsSwitchItem(
        title = stringResource(R.string.before_time),
        checked = state.isBeforeChecked,
        onCheckedChange = { onBeforeToggle() },
        icon = painterResource(R.drawable.ic_builder_before_time),
        dividerBottom = true,
      )
      SettingsSwitchItem(
        title = stringResource(R.string.repeat),
        checked = state.isRepeatChecked,
        onCheckedChange = { onRepeatToggle() },
        icon = painterResource(R.drawable.ic_fluent_arrow_repeat_all),
        dividerBottom = true,
      )
      SettingsSwitchItem(
        title = stringResource(R.string.repeat_limit),
        checked = state.isRepeatLimitChecked,
        onCheckedChange = { onRepeatLimitToggle() },
        icon = painterResource(R.drawable.ic_builder_repeat_limit),
        dividerBottom = true,
      )
      SettingsSwitchItem(
        title = stringResource(R.string.priority),
        checked = state.isPriorityChecked,
        onCheckedChange = { onPriorityToggle() },
        icon = painterResource(R.drawable.ic_fluent_star),
        dividerBottom = true,
      )
      SettingsSwitchItem(
        title = stringResource(R.string.attachment),
        checked = state.isAttachmentChecked,
        onCheckedChange = { onAttachmentToggle() },
        icon = painterResource(R.drawable.ic_builder_attach),
        dividerBottom = true,
      )
      SettingsSwitchItem(
        title = stringResource(R.string.export_to_google_calendar),
        checked = state.isCalendarChecked,
        onCheckedChange = { onCalendarToggle() },
        icon = painterResource(R.drawable.ic_builder_google_calendar_add),
        dividerBottom = true,
      )
      if (state.isTasksRowVisible) {
        SettingsSwitchItem(
          title = stringResource(R.string.add_to_google_tasks),
          checked = state.isTasksChecked,
          onCheckedChange = { onTasksToggle() },
          icon = painterResource(R.drawable.ic_builder_google_task_list),
          dividerBottom = true,
        )
      }
      SettingsSwitchItem(
        title = stringResource(R.string.update_additional_parameters),
        checked = state.isExtraChecked,
        onCheckedChange = { onExtraToggle() },
        icon = painterResource(R.drawable.ic_builder_more_options),
        dividerBottom = true,
      )
      if (state.isLedRowVisible) {
        SettingsSwitchItem(
          title = stringResource(R.string.led_color),
          checked = state.isLedChecked,
          onCheckedChange = { onLedToggle() },
          icon = painterResource(R.drawable.ic_builder_led_color),
          dividerBottom = true,
        )
      }
      if (state.isICalendarRowVisible) {
        SettingsSwitchItem(
          title = stringResource(R.string.builder_icalendar),
          checked = state.isICalendarChecked,
          onCheckedChange = { onICalendarToggle() },
          icon = painterResource(R.drawable.ic_builder_icalendar),
          dividerBottom = true,
        )
      }
      SettingsSwitchItem(
        title = stringResource(R.string.make_call),
        checked = state.isMakeCallChecked,
        onCheckedChange = { onMakeCallToggle() },
        icon = painterResource(R.drawable.ic_builder_add_call),
        dividerBottom = true,
      )
      SettingsSwitchItem(
        title = stringResource(R.string.send_sms),
        checked = state.isSendSmsChecked,
        onCheckedChange = { onSendSmsToggle() },
        icon = painterResource(R.drawable.ic_builder_send_message),
        dividerBottom = true,
      )
      SettingsSwitchItem(
        title = stringResource(R.string.open_app),
        checked = state.isOpenAppChecked,
        onCheckedChange = { onOpenAppToggle() },
        icon = painterResource(R.drawable.ic_builder_add_app),
        dividerBottom = true,
      )
      SettingsSwitchItem(
        title = stringResource(R.string.open_link),
        checked = state.isOpenLinkChecked,
        onCheckedChange = { onOpenLinkToggle() },
        icon = painterResource(R.drawable.ic_builder_web_address),
        dividerBottom = true,
      )
      SettingsSwitchItem(
        title = stringResource(R.string.e_mail),
        checked = state.isSendEmailChecked,
        onCheckedChange = { onSendEmailToggle() },
        icon = painterResource(R.drawable.ic_builder_email_address),
        dividerBottom = true,
      )
    }
  }
}
