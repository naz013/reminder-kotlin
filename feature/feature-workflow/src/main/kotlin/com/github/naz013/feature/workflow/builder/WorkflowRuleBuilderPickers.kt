package com.github.naz013.feature.workflow.builder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.feature.workflow.R
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet
import com.github.naz013.ui.common.compose.foundation.component.BottomSheetHeader
import com.github.naz013.ui.common.compose.foundation.component.BottomSheetItem
import com.github.naz013.ui.common.compose.foundation.component.BottomSheetList
import com.github.naz013.ui.common.compose.foundation.component.NumberStepperField
import com.github.naz013.ui.common.compose.foundation.component.SettingsCheckboxItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.dialog.SingleChoiceDialog

private data class TriggerOption(val id: Int, val template: WorkflowTrigger, val scopeTypes: List<WorkflowScopeType>)

private val TRIGGER_OPTIONS = listOf(
  TriggerOption(1, WorkflowTrigger.ReminderCompleted, WorkflowScopeType.entries),
  TriggerOption(2, WorkflowTrigger.ReminderSnoozedNTimes(count = 3), WorkflowScopeType.entries),
  TriggerOption(3, WorkflowTrigger.GroupAllCompleted, listOf(WorkflowScopeType.GROUP)),
  TriggerOption(4, WorkflowTrigger.LocationEntered, listOf(WorkflowScopeType.REMINDER)),
  TriggerOption(5, WorkflowTrigger.LocationExited, listOf(WorkflowScopeType.REMINDER)),
  TriggerOption(6, WorkflowTrigger.ReminderAgeExceeded(days = 30), WorkflowScopeType.entries),
  TriggerOption(7, WorkflowTrigger.ReminderUnacknowledgedFor(minutes = 30), WorkflowScopeType.entries),
)

private fun needsParams(trigger: WorkflowTrigger): Boolean = when (trigger) {
  is WorkflowTrigger.ReminderSnoozedNTimes,
  is WorkflowTrigger.ReminderAgeExceeded,
  is WorkflowTrigger.ReminderUnacknowledgedFor -> true
  else -> false
}

/** Type picker + inline param sub-form for the "When" slot, filtered to the types that support
 * [scopeType] (e.g. group-completion only offered for a group-scoped rule). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkflowTriggerPickerSheet(
  scopeType: WorkflowScopeType,
  onDismiss: () -> Unit,
  onConfirm: (WorkflowTrigger) -> Unit,
) {
  var configuring by remember { mutableStateOf<WorkflowTrigger?>(null) }
  AppModalBottomSheet(onDismissRequest = onDismiss) {
    BottomSheetHeader(
      title = stringResource(R.string.workflow_builder_choose_trigger),
      showCloseButton = true,
      onCloseClick = onDismiss,
    )
    val current = configuring
    if (current == null) {
      BottomSheetList(
        items = TRIGGER_OPTIONS.filter { scopeType in it.scopeTypes }
          .map { BottomSheetItem(id = it.id, title = workflowTriggerLabel(it.template)) },
        onItemClick = { id ->
          val template = TRIGGER_OPTIONS.first { it.id == id }.template
          if (needsParams(template)) configuring = template else onConfirm(template)
        },
        modifier = Modifier.padding(bottom = 16.dp),
      )
    } else {
      TriggerParamForm(trigger = current, onSave = onConfirm)
    }
  }
}

@Composable
private fun TriggerParamForm(trigger: WorkflowTrigger, onSave: (WorkflowTrigger) -> Unit) {
  when (trigger) {
    is WorkflowTrigger.ReminderSnoozedNTimes -> {
      var count by remember { mutableStateOf(trigger.count.toLong()) }
      ParamStepperForm(
        label = stringResource(R.string.workflow_builder_snooze_count_hint),
        value = count,
        onValueChange = { count = it },
        minValue = 1,
        maxValue = 20,
        onSave = { onSave(WorkflowTrigger.ReminderSnoozedNTimes(count.toInt())) },
      )
    }

    is WorkflowTrigger.ReminderAgeExceeded -> {
      var days by remember { mutableStateOf(trigger.days.toLong()) }
      ParamStepperForm(
        label = stringResource(R.string.workflow_archive_after_days_hint),
        value = days,
        onValueChange = { days = it },
        minValue = 1,
        maxValue = 365,
        onSave = { onSave(WorkflowTrigger.ReminderAgeExceeded(days.toInt())) },
      )
    }

    is WorkflowTrigger.ReminderUnacknowledgedFor -> {
      var minutes by remember { mutableStateOf(trigger.minutes.toLong()) }
      ParamStepperForm(
        label = stringResource(R.string.workflow_builder_minutes_hint),
        value = minutes,
        onValueChange = { minutes = it },
        minValue = 5,
        maxValue = 720,
        step = 5,
        onSave = { onSave(WorkflowTrigger.ReminderUnacknowledgedFor(minutes.toInt())) },
      )
    }

    else -> Unit
  }
}

@Composable
private fun ParamStepperForm(
  label: String,
  value: Long,
  onValueChange: (Long) -> Unit,
  minValue: Long,
  maxValue: Long,
  onSave: () -> Unit,
  step: Long = 1,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
    Text(text = label, style = MaterialTheme.typography.titleSmall)
    NumberStepperField(
      value = value,
      onValueChange = onValueChange,
      minValue = minValue,
      maxValue = maxValue,
      step = step,
      modifier = Modifier.padding(top = 8.dp),
    )
    Button(onClick = onSave, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
      Text(stringResource(R.string.save))
    }
  }
}

private val CONDITION_OPTIONS = listOf(
  WorkflowCondition.PriorityAtLeast(ReminderPriority.HIGH),
  WorkflowCondition.WithinTimeWindow(fromMinuteOfDay = 8 * 60, toMinuteOfDay = 22 * 60),
  WorkflowCondition.GroupIs(groupId = ""),
)

/** Type picker + inline param sub-form for one "If" slot - used both to add a new condition and
 * (via [initial]) to edit an existing one. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkflowConditionPickerSheet(
  groups: List<UiWorkflowGroupOption>,
  initial: WorkflowCondition?,
  onDismiss: () -> Unit,
  onConfirm: (WorkflowCondition) -> Unit,
) {
  var configuring by remember { mutableStateOf(initial) }
  AppModalBottomSheet(onDismissRequest = onDismiss) {
    BottomSheetHeader(
      title = stringResource(R.string.workflow_builder_choose_condition),
      showCloseButton = true,
      onCloseClick = onDismiss,
    )
    val current = configuring
    if (current == null) {
      BottomSheetList(
        items = CONDITION_OPTIONS.mapIndexed { index, template ->
          BottomSheetItem(id = index, title = workflowConditionLabel(template))
        },
        onItemClick = { index -> configuring = CONDITION_OPTIONS[index] },
        modifier = Modifier.padding(bottom = 16.dp),
      )
    } else {
      ConditionParamForm(condition = current, groups = groups, onSave = onConfirm)
    }
  }
}

@Composable
private fun ConditionParamForm(
  condition: WorkflowCondition,
  groups: List<UiWorkflowGroupOption>,
  onSave: (WorkflowCondition) -> Unit,
) {
  when (condition) {
    is WorkflowCondition.PriorityAtLeast -> {
      SelectableTextList(
        items = ReminderPriority.entries.map { it.name to workflowPriorityLabel(it) },
        onSelect = { name -> onSave(WorkflowCondition.PriorityAtLeast(ReminderPriority.valueOf(name))) },
      )
    }

    is WorkflowCondition.WithinTimeWindow -> {
      var fromHour by remember { mutableStateOf((condition.fromMinuteOfDay / 60).toLong()) }
      var fromMinute by remember { mutableStateOf((condition.fromMinuteOfDay % 60).toLong()) }
      var toHour by remember { mutableStateOf((condition.toMinuteOfDay / 60).toLong()) }
      var toMinute by remember { mutableStateOf((condition.toMinuteOfDay % 60).toLong()) }
      Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = stringResource(R.string.from), style = MaterialTheme.typography.titleSmall)
        TimeOfDayFields(
          hour = fromHour,
          minute = fromMinute,
          onHourChange = { fromHour = it },
          onMinuteChange = { fromMinute = it }
        )
        Text(
          text = stringResource(R.string.to),
          style = MaterialTheme.typography.titleSmall,
          modifier = Modifier.padding(top = 12.dp)
        )
        TimeOfDayFields(
          hour = toHour,
          minute = toMinute,
          onHourChange = { toHour = it },
          onMinuteChange = { toMinute = it }
        )
        Button(
          onClick = {
            onSave(
              WorkflowCondition.WithinTimeWindow(
                fromMinuteOfDay = (fromHour * 60 + fromMinute).toInt(),
                toMinuteOfDay = (toHour * 60 + toMinute).toInt(),
              )
            )
          },
          modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) {
          Text(stringResource(R.string.save))
        }
      }
    }

    is WorkflowCondition.GroupIs -> {
      if (groups.isEmpty()) {
        Text(
          text = stringResource(R.string.workflow_builder_no_groups),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )
      } else {
        Text(
          text = stringResource(R.string.workflow_builder_select_group),
          style = MaterialTheme.typography.titleSmall,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        SelectableTextList(
          items = groups.map { it.id to it.title },
          onSelect = { id -> onSave(WorkflowCondition.GroupIs(id)) },
        )
      }
    }
  }
}

@Composable
private fun TimeOfDayFields(
  hour: Long,
  minute: Long,
  onHourChange: (Long) -> Unit,
  onMinuteChange: (Long) -> Unit,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    NumberStepperField(value = hour, onValueChange = onHourChange, minValue = 0, maxValue = 23)
    Text(text = ":", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 4.dp))
    NumberStepperField(value = minute, onValueChange = onMinuteChange, minValue = 0, maxValue = 59, step = 5)
  }
}

private val ACTION_OPTIONS = listOf(
  WorkflowAction.ArchiveReminder,
  WorkflowAction.CompleteReminder,
  WorkflowAction.PurgeReminder,
  WorkflowAction.ApplyNotificationOverride(NotificationSettingsOverride()),
  WorkflowAction.ActivateReminder(reminderId = ""),
)

private fun needsParams(action: WorkflowAction): Boolean =
  action is WorkflowAction.ApplyNotificationOverride || action is WorkflowAction.ActivateReminder

/** Type picker + inline param sub-form for the "Then" slot. [WorkflowAction.RunBackgroundTask] is
 * deliberately excluded - its `taskKey` is an internal Koin DI qualifier with no user-facing
 * catalog of what's safe to expose, so it stays settable only via curated seeded templates. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkflowActionPickerSheet(
  reminders: List<UiWorkflowReminderOption>,
  onDismiss: () -> Unit,
  onConfirm: (WorkflowAction) -> Unit,
) {
  var configuring by remember { mutableStateOf<WorkflowAction?>(null) }
  AppModalBottomSheet(onDismissRequest = onDismiss) {
    BottomSheetHeader(
      title = stringResource(R.string.workflow_builder_choose_action),
      showCloseButton = true,
      onCloseClick = onDismiss,
    )
    val current = configuring
    if (current == null) {
      BottomSheetList(
        items = ACTION_OPTIONS.mapIndexed { index, template ->
          BottomSheetItem(id = index, title = workflowActionLabel(template))
        },
        onItemClick = { index ->
          val template = ACTION_OPTIONS[index]
          if (needsParams(template)) configuring = template else onConfirm(template)
        },
        modifier = Modifier.padding(bottom = 16.dp),
      )
    } else {
      ActionParamForm(action = current, reminders = reminders, onSave = onConfirm)
    }
  }
}

@Composable
private fun ActionParamForm(
  action: WorkflowAction,
  reminders: List<UiWorkflowReminderOption>,
  onSave: (WorkflowAction) -> Unit,
) {
  when (action) {
    is WorkflowAction.ApplyNotificationOverride ->
      NotificationOverrideForm(
        override = action.override,
        onSave = { onSave(WorkflowAction.ApplyNotificationOverride(it)) }
      )

    is WorkflowAction.ActivateReminder -> {
      if (reminders.isEmpty()) {
        Text(
          text = stringResource(R.string.workflow_builder_no_reminders),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )
      } else {
        Text(
          text = stringResource(R.string.workflow_builder_select_reminder),
          style = MaterialTheme.typography.titleSmall,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        SelectableTextList(
          items = reminders.map { it.id to it.title },
          onSelect = { id -> onSave(WorkflowAction.ActivateReminder(id)) },
        )
      }
    }

    else -> Unit
  }
}

@Composable
private fun NotificationOverrideForm(
  override: NotificationSettingsOverride,
  onSave: (NotificationSettingsOverride) -> Unit
) {
  var priority by remember { mutableStateOf(override.priority ?: ReminderPriority.NORMAL) }
  var bypassDnd by remember { mutableStateOf(override.bypassDoNotDisturb ?: false) }
  var wakeScreen by remember { mutableStateOf(override.wakeScreen ?: false) }
  var repeatNotification by remember { mutableStateOf(override.repeatNotification ?: false) }
  var showPriorityDialog by remember { mutableStateOf(false) }

  Column(modifier = Modifier.padding(bottom = 16.dp)) {
    SettingsItem(
      title = stringResource(R.string.priority),
      onClick = { showPriorityDialog = true },
      trailing = { Text(text = workflowPriorityLabel(priority), style = MaterialTheme.typography.titleMedium) },
    )
    SettingsCheckboxItem(
      title = stringResource(R.string.bypass_do_not_disturb),
      checked = bypassDnd,
      onCheckedChange = { bypassDnd = it },
    )
    SettingsCheckboxItem(
      title = stringResource(R.string.wake_screen),
      checked = wakeScreen,
      onCheckedChange = { wakeScreen = it },
    )
    SettingsCheckboxItem(
      title = stringResource(R.string.repeat_notification),
      checked = repeatNotification,
      onCheckedChange = { repeatNotification = it },
    )
    Button(
      onClick = {
        onSave(
          NotificationSettingsOverride(
            priority = priority,
            bypassDoNotDisturb = bypassDnd,
            wakeScreen = wakeScreen,
            repeatNotification = repeatNotification,
          )
        )
      },
      modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp),
    ) {
      Text(stringResource(R.string.save))
    }
  }

  if (showPriorityDialog) {
    val priorities = ReminderPriority.entries
    SingleChoiceDialog(
      title = stringResource(R.string.priority),
      options = priorities.map { workflowPriorityLabel(it) },
      selectedIndex = priorities.indexOf(priority),
      onOptionSelected = { index ->
        priority = priorities[index]
        showPriorityDialog = false
      },
      onDismiss = { showPriorityDialog = false },
    )
  }
}

/** A plain scrollable tap-to-select list keyed by string id - used for groups/reminders, whose
 * count isn't bounded the way the fixed type-option lists above are. */
@Composable
private fun SelectableTextList(items: List<Pair<String, String>>, onSelect: (String) -> Unit) {
  Column(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
    items.forEach { (id, title) ->
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth().clickable { onSelect(id) }.padding(horizontal = 16.dp, vertical = 12.dp),
      )
      HorizontalDivider()
    }
  }
}
