package com.elementary.tasks.reminder.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.editor.GroupValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.SubTasksValueEditor
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.SettingsSectionHeader
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
  onGroupChanged: (GroupBuilderItem) -> Unit,
  onTagToggle: (TagChipState) -> Unit,
  onManageTagsClick: () -> Unit,
  onSaveClick: () -> Unit,
  onExtendClick: () -> Unit,
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
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
        label = { Text(stringResource(R.string.builder_what_you_want_to_remind_you)) },
        modifier = Modifier.fillMaxWidth(),
      )

      SettingsSectionHeader(stringResource(R.string.todo_items))

      state.subTasksItem?.let { subTasksItem ->
        SubTasksValueEditor(
          builderItem = subTasksItem,
          dateTimeManager = dateTimeManager,
          onValueChange = { updated -> onSubTasksChanged(updated as SubTasksBuilderItem) },
        )
      }

      SettingsSectionHeader(stringResource(R.string.choose_group))

      state.groupItem?.let { groupItem ->
        GroupValueEditor(
          builderItem = groupItem,
          onValueChange = { updated -> onGroupChanged(updated as GroupBuilderItem) },
        )
      }

      SettingsSectionHeader(stringResource(R.string.tags))

      TagChipPicker(
        allTags = state.allTags,
        selectedTagIds = state.selectedTagIds,
        onToggle = onTagToggle,
        onManageTagsClick = onManageTagsClick,
        modifier = Modifier.fillMaxWidth(),
      )

      Button(
        onClick = onSaveClick,
        enabled = state.canSave,
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
      ) {
        Text(stringResource(R.string.save))
      }

      OutlinedButton(
        onClick = onExtendClick,
        enabled = state.canSave,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
      ) {
        Text(stringResource(R.string.more_options))
      }
    }
  }
}
