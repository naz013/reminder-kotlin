package com.elementary.tasks.home.agenda

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem

private const val MAIN_TEXT_MAX_LINES = 2
private const val SECONDARY_TEXT_MAX_LINES = 1
private const val TERTIARY_TEXT_MAX_LINES = 2

/**
 * Reusable card scaffold shared by [ReminderAgendaRow] and [BirthdayAgendaRow]: an optional
 * non-clickable status-chip row (e.g. "Enabled") above the title, title/date/tertiary text stack,
 * an optional non-clickable tag-chip row (repeat/remaining/group labels) below, an optional
 * leading slot, and a "more" menu in the top-right.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgendaListItem(
  mainText: String,
  secondaryText: String?,
  tertiaryText: String?,
  tags: List<String>,
  onClick: () -> Unit,
  menuItems: List<PopupMenuItem>,
  onMenuItemClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
  statusChips: List<String> = emptyList(),
  leading: (@Composable () -> Unit)? = null,
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      leading?.let {
        Box(modifier = Modifier.padding(end = 12.dp)) { it() }
      }
      Column(modifier = Modifier.weight(1f)) {
        if (statusChips.isNotEmpty()) {
          AgendaChipRow(chips = statusChips, modifier = Modifier.padding(bottom = 4.dp))
        }
        Text(
          text = mainText,
          style = MaterialTheme.typography.titleMedium,
          maxLines = MAIN_TEXT_MAX_LINES,
          overflow = TextOverflow.Ellipsis,
        )
        secondaryText?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = SECONDARY_TEXT_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
          )
        }
        tertiaryText?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = TERTIARY_TEXT_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
          )
        }
        if (tags.isNotEmpty()) {
          AgendaChipRow(chips = tags, modifier = Modifier.padding(top = 6.dp))
        }
      }
      Box {
        MenuIconButton(
          icon = painterResource(R.drawable.ic_fluent_more_vertical),
          contentDescription = stringResource(R.string.more_options),
          onClick = { menuExpanded = true },
        )
        AppDropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false },
          items = menuItems,
          onItemClick = onMenuItemClick,
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AgendaChipRow(
  chips: List<String>,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    chips.forEach { chip -> AgendaChip(text = chip) }
  }
}

/** A purely decorative, non-clickable chip: no click/ripple semantics, unlike Material3's chips. */
@Composable
private fun AgendaChip(text: String) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.tertiaryContainer,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onTertiaryContainer,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun AgendaListItemPreview() {
  AppTheme {
    AgendaListItem(
      mainText = "Buy milk",
      secondaryText = "Today, 18:00",
      tertiaryText = null,
      tags = listOf("Repeats", "Home"),
      statusChips = listOf("Enabled"),
      onClick = {},
      menuItems = emptyList(),
      onMenuItemClick = {},
    )
  }
}
