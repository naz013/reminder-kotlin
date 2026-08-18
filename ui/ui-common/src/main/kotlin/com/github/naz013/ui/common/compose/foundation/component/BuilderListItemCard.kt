package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.R as UiCommonR

/** Whether a builder row still needs a value, has one, or has a validation error - drives the
 *  small status dot drawn over the leading icon. Mirrors the legacy
 *  `builder_badge_state_empty`/`_ok`/`_error` drawables. */
enum class BuilderItemStatus { EMPTY, DONE, ERROR }

/** Semantics test tag for a [BuilderListItemCard]'s remove button, parameterized by [title] since
 *  several rows (one per active builder item) can be on screen at once and the button itself only
 *  carries an icon (`contentDescription = null`, see that `IconButton`) with no other text to
 *  locate it by. Exposed so instrumented tests can target one specific row's remove button via
 *  `onNodeWithTag(builderItemRemoveTestTag(title))` instead of guessing at the semantics tree. */
fun builderItemRemoveTestTag(title: String): String = "builder_item_remove_$title"

/**
 * A single row in the reminder builder list: leading icon with a status dot, title, a value
 * area, an optional error line, and a remove button. This is the Compose replacement for
 * `list_item_reminder_builder.xml` + `BaseBuilderViewHolder`/`BuilderViewHolder`, kept visually
 * equivalent so the builder list doesn't shift when it's ported.
 *
 * The [value] slot lets callers swap in custom content between the title and the error line -
 * plain text for most rows (see the [String] overload below), or a richer preview (e.g. a note
 * card) for special item types.
 *
 * @param icon Leading 24dp icon for the builder item type.
 * @param title The item's display name (e.g. "Priority").
 * @param status Drives the small status dot drawn over [icon].
 * @param onClick Invoked when the row is tapped to edit its value.
 * @param onRemoveClick Invoked when the trailing remove button is tapped.
 * @param errorText Optional "blocked by ..." message shown below [value] in the error color.
 * @param removeIcon Icon for the trailing remove button.
 * @param value Composable content for the row's value area.
 */
@Composable
fun BuilderListItemCard(
  icon: Painter,
  title: String,
  status: BuilderItemStatus,
  onClick: () -> Unit,
  onRemoveClick: () -> Unit,
  modifier: Modifier = Modifier,
  errorText: String? = null,
  removeIcon: Painter = AppIcons.Builder.Clear,
  value: @Composable () -> Unit = {},
) {
  Card(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
  ) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
      Box(
        modifier = Modifier
          .padding(start = 16.dp, top = 8.dp)
          .size(24.dp),
      ) {
        Icon(
          painter = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(24.dp),
        )
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .size(8.dp)
            .clip(CircleShape)
            .background(status.badgeColor()),
        )
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Box(modifier = Modifier.padding(top = 4.dp)) {
          value()
        }
        if (!errorText.isNullOrEmpty()) {
          Text(
            text = errorText,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp),
          )
        }
      }

      IconButton(
        onClick = onRemoveClick,
        modifier = Modifier
          .padding(top = 4.dp, end = 8.dp)
          .testTag(builderItemRemoveTestTag(title)),
      ) {
        Icon(
          painter = removeIcon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

/** [BuilderListItemCard] overload for the common case of a plain text value line. */
@Composable
fun BuilderListItemCard(
  icon: Painter,
  title: String,
  value: String,
  status: BuilderItemStatus,
  onClick: () -> Unit,
  onRemoveClick: () -> Unit,
  modifier: Modifier = Modifier,
  errorText: String? = null,
  removeIcon: Painter = AppIcons.Builder.Clear,
) {
  BuilderListItemCard(
    icon = icon,
    title = title,
    status = status,
    onClick = onClick,
    onRemoveClick = onRemoveClick,
    modifier = modifier,
    errorText = errorText,
    removeIcon = removeIcon,
    value = {
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
    },
  )
}

@Composable
private fun BuilderItemStatus.badgeColor(): Color = when (this) {
  BuilderItemStatus.EMPTY -> MaterialTheme.colorScheme.outline
  BuilderItemStatus.DONE -> MaterialTheme.colorScheme.primary
  BuilderItemStatus.ERROR -> MaterialTheme.colorScheme.error
}

@Preview(showBackground = true, name = "Builder item - empty")
@Composable
private fun PreviewBuilderListItemCardEmpty() {
  AppTheme {
    BuilderListItemCard(
      icon = painterResource(UiCommonR.drawable.ic_fluent_calendar),
      title = "Date",
      value = "Not selected",
      status = BuilderItemStatus.EMPTY,
      onClick = {},
      onRemoveClick = {},
    )
  }
}

@Preview(showBackground = true, name = "Builder item - done")
@Composable
private fun PreviewBuilderListItemCardDone() {
  AppTheme {
    BuilderListItemCard(
      icon = painterResource(UiCommonR.drawable.ic_fluent_star),
      title = "Priority",
      value = "High",
      status = BuilderItemStatus.DONE,
      onClick = {},
      onRemoveClick = {},
    )
  }
}

@Preview(showBackground = true, name = "Builder item - error")
@Composable
private fun PreviewBuilderListItemCardError() {
  AppTheme {
    BuilderListItemCard(
      icon = painterResource(UiCommonR.drawable.ic_fluent_arrow_repeat_all),
      title = "Repeat",
      value = "Every day",
      status = BuilderItemStatus.ERROR,
      errorText = "Is blocked by: Date",
      onClick = {},
      onRemoveClick = {},
    )
  }
}
