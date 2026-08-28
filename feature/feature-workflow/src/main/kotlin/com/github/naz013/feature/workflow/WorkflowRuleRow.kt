package com.github.naz013.feature.workflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.icon.DrawableCatalog

private const val MENU_ITEM_SAVE_AS_TEMPLATE = 0
private const val MENU_ITEM_DELETE = 1

/** A single active workflow rule row: title, enabled switch, and an overflow menu offering
 * "Save as template" (only for rules not already backed by one) and "Delete". Shared by the
 * Gallery screen's global-rules section and the per-group rules screen. */
@Composable
internal fun WorkflowRuleRow(
  modifier: Modifier = Modifier,
  rule: UiWorkflowRule,
  onEnabledChange: (Boolean) -> Unit,
  onDeleteClick: () -> Unit,
  onSaveAsTemplateClick: () -> Unit,
) {
  var menuExpanded by remember { mutableStateOf(false) }
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
    Spacer(modifier = Modifier.width(8.dp))
    Switch(checked = rule.isEnabled, onCheckedChange = onEnabledChange)
    Box {
      MenuIconButton(
        icon = AppIcons.Fluent.MoreVertical,
        contentDescription = stringResource(R.string.more_options),
        onClick = { menuExpanded = true },
      )
      AppDropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false },
        items = buildList {
          if (rule.canSaveAsTemplate) {
            add(
              PopupMenuItem(
                id = MENU_ITEM_SAVE_AS_TEMPLATE,
                title = stringResource(R.string.workflow_save_as_template),
                iconRes = DrawableCatalog.Fluent.Save,
              )
            )
          }
          add(
            PopupMenuItem(
              id = MENU_ITEM_DELETE,
              title = stringResource(R.string.delete),
              iconRes = DrawableCatalog.Fluent.Delete,
            )
          )
        },
        onItemClick = { id ->
          menuExpanded = false
          when (id) {
            MENU_ITEM_SAVE_AS_TEMPLATE -> onSaveAsTemplateClick()
            MENU_ITEM_DELETE -> onDeleteClick()
          }
        },
      )
    }
  }
  HorizontalDivider()
}

@Preview(showBackground = true)
@Composable
private fun WorkflowRuleRowPreview() {
  AppTheme {
    WorkflowRuleRow(
      rule = UiWorkflowRule(
        id = "1",
        title = "Archive completed reminders after 30 days",
        isEnabled = true,
        canSaveAsTemplate = false
      ),
      onEnabledChange = {},
      onDeleteClick = {},
      onSaveAsTemplateClick = {},
    )
  }
}
