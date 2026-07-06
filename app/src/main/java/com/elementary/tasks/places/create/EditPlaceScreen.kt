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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlaceScreen(
  state: EditPlaceState,
  title: String,
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
        title = { Text(title) },
        navigationIcon = {
          MenuIconButton(
            icon = painterResource(R.drawable.ic_builder_arrow_left),
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        actions = {
          if (state.canDelete) {
            MenuIconButton(
              icon = painterResource(R.drawable.ic_fluent_delete),
              contentDescription = stringResource(R.string.delete),
              onClick = onDeleteClick,
            )
          }
          MenuIconButton(
            icon = painterResource(R.drawable.ic_fluent_save),
            contentDescription = stringResource(R.string.save),
            iconColor = MaterialTheme.colorScheme.tertiary,
            onClick = onSaveClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
  ) { padding ->
    Column(
      modifier = Modifier
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
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
      )
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
      ) {
        mapContent()
      }
    }
  }
}
