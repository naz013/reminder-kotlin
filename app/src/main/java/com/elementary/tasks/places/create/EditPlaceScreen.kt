package com.elementary.tasks.places.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlaceScreen(
  state: EditPlaceState,
  onBackClick: () -> Unit,
  onNameChange: (String) -> Unit,
  onSaveClick: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier,
  mapContent: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(state.screenTitle)) },
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
            onClick = onSaveClick,
            enabled = state.canSave,
          )
          if (state.canDelete) {
            OverflowMenuButton(
              onDeleteClick = onDeleteClick
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
  ) { padding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(padding)
          .background(MaterialTheme.colorScheme.background),
    ) {
      OutlinedTextField(
        value = state.name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.name)) },
        isError = state.nameError,
        supportingText = {
          if (state.nameError) {
            Text(stringResource(R.string.must_be_not_empty))
          }
        },
        singleLine = true,
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
      )
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .weight(1f),
      ) {
        mapContent()
      }
    }
  }
}

@Composable
private fun OverflowMenuButton(
  onDeleteClick: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val actions =
    listOf(
      Triple(0, stringResource(R.string.action_delete), R.drawable.ic_fluent_delete) to onDeleteClick,
    )
  Box {
    MenuIconButton(
      icon = painterResource(R.drawable.ic_fluent_more_vertical),
      contentDescription = stringResource(R.string.more_options),
      onClick = { expanded = true },
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items = actions.map { (triple, _) -> PopupMenuItem(id = triple.first, title = triple.second, iconRes = triple.third) },
      onItemClick = { id -> actions.firstOrNull { it.first.first == id }?.second?.invoke() },
    )
  }
}
