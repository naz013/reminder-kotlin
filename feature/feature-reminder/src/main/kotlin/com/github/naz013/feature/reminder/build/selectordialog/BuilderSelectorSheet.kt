package com.github.naz013.feature.reminder.build.selectordialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.preset.UiPresetList
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.UiSelectorItem
import com.github.naz013.feature.reminder.build.UiSelectorItemState
import com.github.naz013.feature.reminder.build.preset.PresetListItem
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet
import com.github.naz013.ui.common.compose.foundation.component.SearchBar

private val LIST_MAX_HEIGHT = 420.dp

/**
 * The "add builder item" picker: a modal bottom sheet with a search field, an optional tab row
 * (params / presets / recur presets - hidden when only one tab applies), and a scrollable list
 * for whichever tab is selected. This is the Compose replacement for `SelectorDialog`
 * (a `BottomSheetDialogFragment`); since it now lives in the same composition as the builder
 * screen instead of a separate fragment, selection is reported directly via callbacks instead of
 * the old `SelectorDialogCommunicator` singleton.
 *
 * @param tabs Which tabs to show, and in what order - computed by
 * [SelectorDialogDataHolder.getTabs].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BuilderSelectorSheet(
  tabs: List<SelectorTab>,
  builderItems: List<UiSelectorItem>,
  presets: List<UiPresetList>,
  recurPresets: List<UiPresetList>,
  onDismissRequest: () -> Unit,
  onBuilderItemSelected: (BuilderItem<*>) -> Unit,
  onPresetSelected: (UiPresetList) -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedTab by remember(tabs) { mutableStateOf(tabs.firstOrNull() ?: SelectorTab.BUILDER) }
  var query by remember { mutableStateOf("") }
  val lowerQuery = query.trim().lowercase()

  AppModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(R.string.select),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      IconButton(onClick = onDismissRequest) {
        Icon(
          painter = painterResource(R.drawable.ic_builder_chevron_down),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    }

    SearchBar(
      query = query,
      onQueryChange = { query = it },
      placeholder = stringResource(selectedTab.searchHintRes),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
    )

    if (tabs.size > 1) {
      val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
      SecondaryTabRow(selectedTabIndex = selectedIndex) {
        tabs.forEach { tab ->
          Tab(
            selected = tab == selectedTab,
            onClick = {
              selectedTab = tab
              query = ""
            },
            text = { Text(stringResource(tab.titleRes)) },
          )
        }
      }
    }

    when (selectedTab) {
      SelectorTab.BUILDER -> {
        val filtered = remember(builderItems, lowerQuery) {
          if (lowerQuery.isEmpty()) {
            builderItems
          } else {
            builderItems.filter {
              it.builderItem.title.lowercase().contains(lowerQuery) ||
                it.builderItem.description?.lowercase()?.contains(lowerQuery) == true
            }
          }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = LIST_MAX_HEIGHT)) {
          items(filtered, key = { it.builderItem.biType }) { item ->
            SelectorItemRow(item = item, onClick = { onBuilderItemSelected(item.builderItem) })
          }
        }
      }

      SelectorTab.PRESETS -> {
        PresetList(
          presets = presets,
          query = lowerQuery,
          onPresetSelected = onPresetSelected,
        )
      }

      SelectorTab.RECUR_PRESETS -> {
        PresetList(
          presets = recurPresets,
          query = lowerQuery,
          onPresetSelected = onPresetSelected,
        )
      }
    }
  }
}

@Composable
private fun PresetList(
  presets: List<UiPresetList>,
  query: String,
  onPresetSelected: (UiPresetList) -> Unit,
  modifier: Modifier = Modifier,
) {
  val filtered = remember(presets, query) {
    if (query.isEmpty()) presets else presets.filter { it.name.lowercase().contains(query) }
  }
  LazyColumn(modifier = modifier.fillMaxWidth().heightIn(max = LIST_MAX_HEIGHT)) {
    items(filtered, key = { it.id }) { preset ->
      PresetListItem(preset = preset, onClick = { onPresetSelected(preset) }, canDelete = false)
    }
  }
}

@Composable
private fun SelectorItemRow(
  item: UiSelectorItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val available = item.state is UiSelectorItemState.UiSelectorAvailable
  val contentAlpha = if (available) 1f else 0.75f

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .then(if (available) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    ) {
      Icon(
        painter = painterResource(item.builderItem.iconRes),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
        modifier = Modifier.padding(top = 4.dp).size(24.dp),
      )
      Column(modifier = Modifier.padding(start = 16.dp)) {
        Text(
          text = item.builderItem.title,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
        )
        val description = item.builderItem.description
        if (!description.isNullOrEmpty()) {
          Text(
            text = description,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
            modifier = Modifier.padding(top = 4.dp),
          )
        }
        if (!item.requiredMessage.isNullOrEmpty()) {
          Text(
            text = item.requiredMessage,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(top = 4.dp),
          )
        }
        val state = item.state
        if (state is UiSelectorItemState.UiSelectorUnavailable) {
          Text(
            text = state.message,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp),
          )
        }
      }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp, end = 16.dp))
  }
}
