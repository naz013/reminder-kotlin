package com.github.naz013.feature.reminder.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.group.UiGroupList
import com.github.naz013.feature.reminder.build.SubTasksBuilderItem
import com.github.naz013.feature.reminder.build.valuedialog.editor.SubTasksValueEditor
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.tag.TagChipPicker
import com.github.naz013.ui.tag.TagChipState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditScreen(
  state: TodoEditState,
  dateTimeManager: DateTimeManager,
  onBackClick: () -> Unit,
  onTitleChange: (String) -> Unit,
  onSubTasksChanged: (SubTasksBuilderItem) -> Unit,
  onGroupSelected: (UiGroupList?) -> Unit,
  onTagToggle: (TagChipState) -> Unit,
  onManageTagsClick: () -> Unit,
  onSaveClick: () -> Unit,
  onExtendClick: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.todo)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        actions = {
          MenuTextButton(
            text = stringResource(R.string.save),
            enabled = state.canSave,
            onClick = onSaveClick,
          )
          if (state.isEditing) {
            IconButton(onClick = onDeleteClick) {
              Icon(
                painter = painterResource(R.drawable.ic_fluent_delete),
                contentDescription = stringResource(R.string.delete),
              )
            }
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
          .background(MaterialTheme.colorScheme.background)
          .padding(padding)
          .verticalScroll(rememberScrollState())
          .padding(16.dp),
    ) {
      OutlinedTextField(
        value = state.title,
        onValueChange = onTitleChange,
        label = { Text(stringResource(R.string.title)) },
        modifier = Modifier.fillMaxWidth(),
      )

      TodoSectionHeader(stringResource(R.string.todo_items))

      state.subTasksItem?.let { subTasksItem ->
        SubTasksValueEditor(
          builderItem = subTasksItem,
          dateTimeManager = dateTimeManager,
          onValueChange = { updated -> onSubTasksChanged(updated as SubTasksBuilderItem) },
        )
      }

      TodoSectionHeader(stringResource(R.string.group))

      GroupChipRow(
        availableGroups = state.availableGroups,
        selectedGroup = state.selectedGroup,
        onGroupSelected = onGroupSelected,
      )

      TodoSectionHeader(stringResource(R.string.tags))

      TagChipPicker(
        allTags = state.allTags,
        selectedTagIds = state.selectedTagIds,
        onToggle = onTagToggle,
        onManageTagsClick = onManageTagsClick,
        modifier = Modifier.fillMaxWidth(),
      )

      FilledTonalButton(
        onClick = onExtendClick,
        enabled = state.canSave,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
      ) {
        Text(stringResource(R.string.more_options))
      }
    }
  }
}

/** Local, screen-scoped section label - intentionally not `SettingsSectionHeader`, which is
 *  reserved for Settings screens. */
@Composable
private fun TodoSectionHeader(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.tertiary,
    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupChipRow(
  availableGroups: List<UiGroupList>,
  selectedGroup: UiGroupList?,
  onGroupSelected: (UiGroupList?) -> Unit,
) {
  FlowRow(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    FilterChip(
      selected = selectedGroup == null,
      onClick = { onGroupSelected(null) },
      label = { Text(stringResource(R.string.smart_list_no_group)) },
    )
    availableGroups.forEach { group ->
      FilterChip(
        selected = selectedGroup?.id == group.id,
        onClick = { onGroupSelected(group) },
        label = { Text(group.title) },
      )
    }
  }
}
