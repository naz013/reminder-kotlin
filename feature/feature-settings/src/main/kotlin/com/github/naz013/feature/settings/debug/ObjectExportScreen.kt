package com.github.naz013.feature.settings.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ObjectExportScreen(
  state: ObjectExportState,
  onObjectTypeSelected: (ObjectExportType) -> Unit,
  onItemClick: (ObjectExportItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
  ) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
      expanded = isExpanded,
      onExpandedChange = { isExpanded = it },
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(16.dp),
    ) {
      OutlinedTextField(
        value = state.objectType.name,
        onValueChange = {},
        readOnly = true,
        label = { Text("Object type") },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
        modifier =
          Modifier
            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            .fillMaxWidth(),
      )
      DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = { isExpanded = false },
        modifier = Modifier.exposedDropdownSize(),
      ) {
        ObjectExportType.entries.forEach { type ->
          DropdownMenuItem(
            text = { Text(type.name) },
            onClick = {
              isExpanded = false
              onObjectTypeSelected(type)
            },
          )
        }
      }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(state.items) { item ->
        Text(
          text = item.title,
          style = MaterialTheme.typography.titleMedium,
          modifier =
            Modifier
              .fillMaxWidth()
              .clickable { onItemClick(item) }
              .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        HorizontalDivider()
      }
    }
  }
}
