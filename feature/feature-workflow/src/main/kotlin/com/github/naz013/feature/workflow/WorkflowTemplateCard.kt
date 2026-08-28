package com.github.naz013.feature.workflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.domain.workflow.WorkflowTemplateCategory
import com.github.naz013.ui.common.compose.AppTheme

/** A single template gallery entry: title/description plus an "Apply" button, disabled (with an
 * explanatory label) when the template doesn't support the current entry point's scope type. */
@Composable
internal fun WorkflowTemplateCard(
  modifier: Modifier = Modifier,
  template: UiWorkflowTemplate,
  applyButtonLabel: String,
  onApplyClick: () -> Unit,
) {
  Card(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = template.title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      if (!template.description.isNullOrEmpty()) {
        Text(
          text = template.description,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
      TextButton(onClick = onApplyClick, enabled = template.canApply) {
        Text(
          when {
            template.canApply -> applyButtonLabel
            template.alreadyApplied -> stringResource(R.string.workflow_already_applied)
            else -> stringResource(R.string.workflow_open_group_to_apply)
          }
        )
      }
    }
  }
}

/** Human-readable label for a template category, matching the section headers grouping the
 * gallery — one string resource per [WorkflowTemplateCategory] value. */
@Composable
fun workflowCategoryTitle(category: WorkflowTemplateCategory): String = when (category) {
  WorkflowTemplateCategory.REMINDER_LIFECYCLE -> stringResource(R.string.workflow_category_reminder_lifecycle)
  WorkflowTemplateCategory.GROUP -> stringResource(R.string.workflow_category_group)
  WorkflowTemplateCategory.LOCATION -> stringResource(R.string.workflow_category_location)
  WorkflowTemplateCategory.NOTIFICATION_ESCALATION -> stringResource(R.string.workflow_category_notification_escalation)
  WorkflowTemplateCategory.SYSTEM_INTEGRATION -> stringResource(R.string.workflow_category_system_integration)
  WorkflowTemplateCategory.PRIVACY_DATA_HYGIENE -> stringResource(R.string.workflow_category_privacy_data_hygiene)
}

@Preview(showBackground = true)
@Composable
private fun WorkflowTemplateCardPreview() {
  AppTheme {
    Column {
      WorkflowTemplateCard(
        template = UiWorkflowTemplate(
          id = "1",
          title = "Archive completed reminders after 30 days",
          description = "Automatically archives a reminder once it's been completed for a while.",
          category = WorkflowTemplateCategory.REMINDER_LIFECYCLE,
          canApply = true,
          alreadyApplied = false,
        ),
        applyButtonLabel = "Apply globally",
        onApplyClick = {},
      )
      WorkflowTemplateCard(
        template = UiWorkflowTemplate(
          id = "2",
          title = "Archive group once everything is completed",
          description = "Archives every reminder in a group once none of its reminders are still active.",
          category = WorkflowTemplateCategory.GROUP,
          canApply = false,
          alreadyApplied = false,
        ),
        applyButtonLabel = "Apply globally",
        onApplyClick = {},
      )
      WorkflowTemplateCard(
        template = UiWorkflowTemplate(
          id = "3",
          title = "Escalate after 3 repeated snoozes",
          description = "Bypasses Do Not Disturb and raises priority once a reminder is snoozed too often.",
          category = WorkflowTemplateCategory.NOTIFICATION_ESCALATION,
          canApply = false,
          alreadyApplied = true,
        ),
        applyButtonLabel = "Apply globally",
        onApplyClick = {},
      )
    }
  }
}
