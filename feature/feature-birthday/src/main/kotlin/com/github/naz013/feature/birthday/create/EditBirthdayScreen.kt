package com.github.naz013.feature.birthday.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.common.compose.foundation.component.FormItem
import com.github.naz013.ui.common.compose.foundation.component.FormSwitchItem
import com.github.naz013.ui.common.compose.foundation.component.PhoneNumberVisualTransformation
import com.github.naz013.ui.common.compose.foundation.component.SettingsSectionHeader
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.tag.TagChipPicker
import com.github.naz013.ui.tag.TagChipState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditBirthdayScreen(
  modifier: Modifier = Modifier,
  state: EditBirthdayState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onDeleteMenuClick: () -> Unit,
  onNameChange: (String) -> Unit,
  onYearCheckChanged: (Boolean) -> Unit,
  onDateFieldClick: () -> Unit,
  onNumberChange: (String) -> Unit,
  onPickContactClick: () -> Unit,
  onDeleteConfirmed: () -> Unit,
  onCopyKeepClick: () -> Unit,
  onCopyReplaceClick: () -> Unit,
  onDialogDismiss: () -> Unit,
  onTagToggle: (TagChipState) -> Unit,
  onManageTagsClick: () -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(if (state.hasId) R.string.edit_birthday else R.string.add_birthday)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            enabled = !state.isLoading,
            onClick = onBackClick,
          )
        },
        actions = {
          if (state.canDelete) {
            MenuIconButton(
              icon = painterResource(R.drawable.ic_fluent_delete),
              contentDescription = stringResource(R.string.delete),
              enabled = !state.isLoading,
              onClick = onDeleteMenuClick,
            )
          }
          MenuTextButton(
            text = stringResource(R.string.save),
            enabled = !state.isLoading,
            onClick = onSaveClick,
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
        value = state.name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.name)) },
        isError = state.nameError,
        supportingText = {
          if (state.nameError) Text(stringResource(R.string.must_be_not_empty))
        },
        singleLine = true,
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth(),
      )

      Card(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      ) {
        Column {
          FormSwitchItem(
            title = stringResource(R.string.i_don_t_know_the_year),
            checked = state.ignoreYear,
            onCheckedChange = onYearCheckChanged,
            enabled = !state.isLoading,
            dividerBottom = true,
          )
          FormItem(
            title = stringResource(R.string.select_date),
            enabled = !state.isLoading,
            onClick = onDateFieldClick,
            trailing = {
              Text(text = state.dateText, style = MaterialTheme.typography.titleMedium)
            },
          )
        }
      }

      SettingsSectionHeader(stringResource(R.string.tags))

      TagChipPicker(
        allTags = state.allTags,
        selectedTagIds = state.selectedTagIds,
        onToggle = onTagToggle,
        onManageTagsClick = onManageTagsClick,
        modifier = Modifier.fillMaxWidth(),
      )

      SettingsSectionHeader(stringResource(R.string.attach_contact))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
      ) {
        OutlinedTextField(
          value = state.number,
          onValueChange = onNumberChange,
          label = { Text(stringResource(R.string.phone)) },
          singleLine = true,
          enabled = !state.isLoading,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
          visualTransformation = PhoneNumberVisualTransformation,
          modifier = Modifier.weight(1f),
        )
        MenuIconButton(
          icon = painterResource(R.drawable.ic_fluent_contacts),
          contentDescription = stringResource(R.string.acc_select_number_from_contacts),
          enabled = !state.isLoading,
          onClick = onPickContactClick,
          modifier = Modifier.padding(start = 8.dp),
        )
      }

      if (state.contactName != null) {
        Card(
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(top = 16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
          ) {
            val photo = state.contactPhoto
            if (photo != null) {
              Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = stringResource(R.string.acc_contact_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(36.dp).clip(CircleShape),
              )
            } else {
              Icon(
                painter = painterResource(R.drawable.ic_fluent_person),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
              )
            }
            Text(
              text = state.contactName,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.padding(start = 16.dp),
            )
          }
        }
      }

      adsContent()
    }
  }

  val dialogDispatcher = rememberDialogDispatcher()

  LaunchedEffect(state.dialog) {
    if (state.dialog == EditBirthdayDialog.DeleteConfirm) {
      dialogDispatcher.showDialog(
        textRes = R.string.are_you_sure,
        positiveButtonRes = R.string.yes,
        negativeButtonRes = R.string.no,
        onPositive = onDeleteConfirmed,
        onNegative = onDialogDismiss,
      )
    }
  }

  // Kept as a plain AlertDialog rather than DialogDispatcher: DialogDispatcher maps an
  // outside-tap dismiss to the negative button's action, which here would silently trigger the
  // destructive "replace" action instead of a no-op cancel.
  if (state.dialog == EditBirthdayDialog.CopyConflict) {
    AlertDialog(
      onDismissRequest = onDialogDismiss,
      text = { Text(stringResource(R.string.same_birthday_message)) },
      confirmButton = {
        TextButton(onClick = onCopyKeepClick) { Text(stringResource(R.string.keep)) }
      },
      dismissButton = {
        TextButton(onClick = onCopyReplaceClick) { Text(stringResource(R.string.replace)) }
      },
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun EditBirthdayScreenPreview() {
  AppTheme {
    EditBirthdayScreen(
      state =
        EditBirthdayState(
          name = "Test User",
          dateText = "25 May, 2000",
          hasId = true,
          canDelete = true,
        ),
      onBackClick = {},
      onSaveClick = {},
      onDeleteMenuClick = {},
      onNameChange = {},
      onYearCheckChanged = {},
      onDateFieldClick = {},
      onNumberChange = {},
      onPickContactClick = {},
      onDeleteConfirmed = {},
      onCopyKeepClick = {},
      onCopyReplaceClick = {},
      onDialogDismiss = {},
      onTagToggle = {},
      onManageTagsClick = {},
      adsContent = {},
    )
  }
}
