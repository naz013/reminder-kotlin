package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.TimeZoneBuilderItem
import com.github.naz013.feature.reminder.build.timezone.UiTimeZoneOption
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.compose.foundation.component.SearchBar

private val LIST_MAX_HEIGHT = 360.dp

/** Searchable single-select list of time zones; the first row is the device (floating) option. */
@Composable
internal fun TimeZoneValueEditor(
  builderItem: TimeZoneBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  modifier: Modifier = Modifier,
) {
  var query by remember(builderItem) { mutableStateOf("") }
  var selectedId by remember(builderItem) { mutableStateOf(builderItem.modifier.getValue()) }
  val options = remember(builderItem, query) { builderItem.catalog.options(query) }

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    SearchBar(
      modifier = Modifier.fillMaxWidth(),
      query = query,
      onQueryChange = { query = it },
      placeholder = stringResource(R.string.time_zone_search_hint),
    )
    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = LIST_MAX_HEIGHT)) {
      items(options, key = { it.id ?: "" }) { option ->
        TimeZoneRow(
          option = option,
          selected = option.id == selectedId,
          onClick = {
            selectedId = option.id
            builderItem.modifier.update(option.id)
            onValueChange(builderItem)
          },
        )
      }
    }
  }
}

@Composable
private fun TimeZoneRow(
  option: UiTimeZoneOption,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = if (selected) AppIcons.Fluent.Checkmark else AppIcons.Fluent.RadioButton,
      contentDescription = null,
      tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(end = 12.dp).size(24.dp),
    )
    Text(
      text = option.title,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f),
    )
    if (option.offset.isNotEmpty()) {
      Text(
        text = option.offset,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 12.dp),
      )
    }
  }
}
