package com.elementary.tasks.workflow

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.AnchoredPopupMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem

private const val MENU_ITEM_SAVE_AS_TEMPLATE = 0
private const val MENU_ITEM_DELETE = 1

/** A single active workflow rule row: title, enabled switch, and an overflow menu offering
 * "Save as template" (only for rules not already backed by one) and "Delete". Shared by the
 * Gallery screen's global-rules section and the per-group rules screen. */
@Composable
fun WorkflowRuleRow(
  rule: UiWorkflowRule,
  onEnabledChange: (Boolean) -> Unit,
  onDeleteClick: () -> Unit,
  onSaveAsTemplateClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = rule.title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f),
    )
    Switch(checked = rule.isEnabled, onCheckedChange = onEnabledChange)
    AnchoredPopupMenu(
      items = buildList {
        if (rule.canSaveAsTemplate) {
          add(
            PopupMenuItem(
              id = MENU_ITEM_SAVE_AS_TEMPLATE,
              title = stringResource(R.string.workflow_save_as_template),
              icon = Icons.Default.Save,
            )
          )
        }
        add(
          PopupMenuItem(
            id = MENU_ITEM_DELETE,
            title = stringResource(R.string.delete),
            icon = Icons.Default.Delete,
          )
        )
      },
      onItemClick = { id ->
        when (id) {
          MENU_ITEM_SAVE_AS_TEMPLATE -> onSaveAsTemplateClick()
          MENU_ITEM_DELETE -> onDeleteClick()
        }
      },
    ) {
      IconButton(onClick = {}) {
        Icon(imageVector = Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
      }
    }
  }
  HorizontalDivider()
}

@Preview(showBackground = true)
@Composable
private fun WorkflowRuleRowPreview() {
  AppTheme {
    WorkflowRuleRow(
      rule = UiWorkflowRule(id = "1", title = "Archive completed reminders after 30 days", isEnabled = true, canSaveAsTemplate = false),
      onEnabledChange = {},
      onDeleteClick = {},
      onSaveAsTemplateClick = {},
    )
  }
}
