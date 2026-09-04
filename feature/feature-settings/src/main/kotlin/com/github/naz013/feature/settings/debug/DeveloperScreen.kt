package com.github.naz013.feature.settings.debug

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
internal fun DeveloperScreen(
  state: DeveloperState,
  modifier: Modifier = Modifier,
  onResetBannersClick: () -> Unit,
  onResetOnboardingClick: () -> Unit,
  onBirthdayDialogClick: () -> Unit,
  onReminderDialogClick: () -> Unit,
  onRecurrenceTestClick: () -> Unit,
  onObjectExportClick: () -> Unit,
  onReviewDialogClick: () -> Unit,
  onProVersionClick: () -> Unit,
  onClearTableClick: () -> Unit,
  onClearAllTablesClick: () -> Unit,
  onClearAllTablesConfirm: () -> Unit,
  onClearAllTablesDismiss: () -> Unit,
  onInsertDemoDataClick: () -> Unit,
  onInsertHugeFormattedNotesClick: () -> Unit,
  onInsertInsightsDemoDataClick: () -> Unit,
  onPopulateCalendarNormalDataClick: () -> Unit,
  onPopulateCalendarMassiveDataClick: () -> Unit,
  onDialogOptionSelected: (Int) -> Unit,
  onDialogConfirm: () -> Unit,
  onDialogDismiss: () -> Unit,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
  ) {
    DeveloperOption(
      title = "Reset banners state on Home Screen",
      subtitle = "Shows the privacy, login and what's new banners again",
      onClick = onResetBannersClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Reset Onboarding State",
      subtitle = "Shows the first-run onboarding flow again on the next app launch",
      onClick = onResetOnboardingClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Open Birthday Screen",
      subtitle = "Save a mock birthday and open its action screen",
      onClick = onBirthdayDialogClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Open Reminder Screen",
      subtitle = "Save a mock reminder and open its action screen",
      onClick = onReminderDialogClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Start Recurrence Rule Test Reminders",
      subtitle = "Pick a delay and a recurrence type, then create and activate a real test reminder",
      onClick = onRecurrenceTestClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Save object to File",
      subtitle = "Export a sample domain object to a file",
      onClick = onObjectExportClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Show Review Dialog",
      subtitle = "Preview the in-app review request form",
      onClick = onReviewDialogClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Open PRO Version Screen",
      subtitle = "Preview the PRO version advertisement screen",
      onClick = onProVersionClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Clear DB Table",
      subtitle = "Pick a database table and delete all of its rows",
      onClick = onClearTableClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Clear All DB Tables",
      subtitle = "Delete all rows from every table in the database",
      onClick = onClearAllTablesClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Insert Demo Data",
      subtitle = "Adds sample reminders, birthdays and notes, useful for taking promo screenshots",
      onClick = onInsertDemoDataClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Insert Huge Formatted Notes",
      subtitle = "Adds a few large notes with thousands of overlapping bold/italic/underline/font/color/gradient spans and mixed headings/bullets, to stress-test the rich-text editor and renderer",
      onClick = onInsertHugeFormattedNotesClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Insert Insights Demo Data",
      subtitle = "Adds habit reminders with fabricated fire history, so the Insights screen has streaks and charts to show",
      onClick = onInsertInsightsDemoDataClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Populate Calendar (Normal)",
      subtitle = "Adds dozens of reminders with different recurrence and a few birthdays, with occurrences, to populate the calendar views",
      onClick = onPopulateCalendarNormalDataClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Populate Calendar (Massive)",
      subtitle = "Same as above but with a massive amount of reminders and birthdays, to stress-test the Month/Day/3-day/7-day views",
      onClick = onPopulateCalendarMassiveDataClick,
    )
    HorizontalDivider()
  }

  val dialog = state.dialog
  if (dialog != null) {
    DeveloperChoiceDialog(
      dialog = dialog,
      onOptionSelected = onDialogOptionSelected,
      onConfirm = onDialogConfirm,
      onDismiss = onDialogDismiss,
    )
  }

  if (state.clearAllTablesConfirmation) {
    AlertDialog(
      onDismissRequest = onClearAllTablesDismiss,
      title = { Text("Clear all DB tables?") },
      text = { Text("This will permanently delete all data from every table in the database. This action cannot be undone.") },
      confirmButton = { TextButton(onClick = onClearAllTablesConfirm) { Text("Clear all") } },
      dismissButton = { TextButton(onClick = onClearAllTablesDismiss) { Text("Cancel") } },
    )
  }
}

@Composable
private fun DeveloperOption(
  title: String,
  subtitle: String,
  onClick: () -> Unit,
) {
  ListItem(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
    headlineContent = { Text(text = title) },
    supportingContent = { Text(text = subtitle) },
  )
}

@Composable
private fun DeveloperChoiceDialog(
  dialog: DeveloperChoiceDialog,
  onOptionSelected: (Int) -> Unit,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  val title =
    when (dialog.kind) {
      DeveloperDialogKind.CLEAR_TABLE -> "Select table to clear"
      DeveloperDialogKind.REMINDER, DeveloperDialogKind.BIRTHDAY -> "Select action to test"
      DeveloperDialogKind.RECURRENCE_TEST -> "Select time until reminder fires"
      DeveloperDialogKind.RECURRENCE_TEST_TYPE -> "Select recurrence type to test"
    }
  val confirmText =
    when (dialog.kind) {
      DeveloperDialogKind.CLEAR_TABLE -> "Clear"
      DeveloperDialogKind.REMINDER, DeveloperDialogKind.BIRTHDAY -> "Run"
      DeveloperDialogKind.RECURRENCE_TEST -> "Next"
      DeveloperDialogKind.RECURRENCE_TEST_TYPE -> "Start"
    }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column(
        modifier =
          Modifier
            .heightIn(max = 400.dp)
            .selectableGroup()
            .verticalScroll(rememberScrollState()),
      ) {
        dialog.options.forEachIndexed { index, option ->
          val selected = index == dialog.selectedIndex
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
              Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = { onOptionSelected(index) }, role = Role.RadioButton)
                .padding(vertical = 8.dp),
          ) {
            RadioButton(selected = selected, onClick = null)
            Text(
              text = option,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.padding(start = 8.dp),
            )
          }
        }
      }
    },
    confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}
