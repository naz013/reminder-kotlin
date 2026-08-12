package com.github.naz013.feature.workflow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.domain.workflow.WorkflowTemplateCategory
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.SettingsSectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowRulesForGroupScreen(
  state: WorkflowRulesForGroupState,
  onBackClick: () -> Unit,
  onRuleEnabledChange: (String, Boolean) -> Unit,
  onDeleteRuleClick: (String) -> Unit,
  onSaveRuleAsTemplateClick: (String) -> Unit,
  onApplyTemplateClick: (String) -> Unit,
  onCreateRuleClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.workflow_rules)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onCreateRuleClick) {
        Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.workflow_create_custom_rule))
      }
    },
  ) { padding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
      item { SettingsSectionHeader(stringResource(R.string.workflow_rules_for_group)) }
      if (state.rules.isEmpty()) {
        item {
          Text(
            text = stringResource(R.string.workflow_no_rules_yet),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          )
        }
      } else {
        items(state.rules, key = { it.id }) { rule ->
          WorkflowRuleRow(
            rule = rule,
            onEnabledChange = { onRuleEnabledChange(rule.id, it) },
            onDeleteClick = { onDeleteRuleClick(rule.id) },
            onSaveAsTemplateClick = { onSaveRuleAsTemplateClick(rule.id) },
          )
        }
      }

      item { SettingsSectionHeader(stringResource(R.string.workflow_templates)) }
      state.templatesByCategory.forEach { (category, templates) ->
        item {
          Text(
            text = workflowCategoryTitle(category),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
          )
        }
        items(templates, key = { it.id }) { template ->
          WorkflowTemplateCard(
            template = template,
            applyButtonLabel = stringResource(R.string.workflow_apply_to_group),
            onApplyClick = { onApplyTemplateClick(template.id) },
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun WorkflowRulesForGroupScreenPreview() {
  AppTheme {
    WorkflowRulesForGroupScreen(
      state = WorkflowRulesForGroupState(
        isLoading = false,
        rules = listOf(
          UiWorkflowRule(
            id = "1",
            title = "Archive group once everything is completed",
            isEnabled = true,
            canSaveAsTemplate = false
          ),
        ),
        templatesByCategory = mapOf(
          WorkflowTemplateCategory.GROUP to listOf(
            UiWorkflowTemplate(
              id = "2",
              title = "Archive group once everything is completed",
              description = "Archives every reminder in a group once none of its reminders are still active.",
              category = WorkflowTemplateCategory.GROUP,
              canApply = true,
            ),
          ),
        ),
      ),
      onBackClick = {},
      onRuleEnabledChange = { _, _ -> },
      onDeleteRuleClick = {},
      onSaveRuleAsTemplateClick = {},
      onApplyTemplateClick = {},
      onCreateRuleClick = {},
    )
  }
}
