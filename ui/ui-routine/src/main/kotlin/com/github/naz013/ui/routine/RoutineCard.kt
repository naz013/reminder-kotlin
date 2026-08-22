package com.github.naz013.ui.routine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme

private const val TITLE_MAX_LINES = 1
private const val DESCRIPTION_MAX_LINES = 2
private val CONTENT_PADDING = 12.dp
private val BADGE_ROW_SPACING = 8.dp
private val BADGE_ICON_SIZE = 14.dp

/**
 * Solid-colored routine card (mirrors [com.github.naz013.ui.note.NoteCard]'s "one shared card
 * everywhere" role for Notes) - title, optional description, step-count/duration/schedule badges,
 * an optional caller-supplied tag row, and a "Start" CTA. Deliberately takes no `Routine`/
 * `RoutineStep` domain type or tag data directly: `ui-routine` can only depend on `ui-common`
 * (see `docs/architecture.md`'s `ui-*` dependency rule), so [tagsContent] lets the feature layer
 * render `ui-tag`'s `TagChipRow` inline without `ui-routine` needing to depend on `ui-tag` itself.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoutineCard(
  modifier: Modifier = Modifier,
  routine: UiRoutineListItem,
  startButtonLabel: String,
  onClick: () -> Unit,
  onStartClick: () -> Unit,
  onLongClick: (() -> Unit)? = null,
  border: BorderStroke? = null,
  tagsContent: @Composable (() -> Unit)? = null,
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.medium)
      .combinedClickable(onClick = onClick, onLongClick = onLongClick)
      .testTag("routine_card_${routine.id}"),
    colors = CardDefaults.cardColors(containerColor = routine.backgroundColor),
    border = border,
  ) {
    Column(modifier = Modifier.padding(CONTENT_PADDING)) {
      Row(verticalAlignment = Alignment.Top) {
        routine.iconRes?.let {
          Icon(
            painter = painterResource(it),
            contentDescription = null,
            tint = routine.contentColor,
            modifier = Modifier.size(18.dp).padding(end = 6.dp),
          )
        }
        Text(
          text = routine.title,
          color = routine.contentColor,
          style = MaterialTheme.typography.titleMedium,
          maxLines = TITLE_MAX_LINES,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        if (routine.isPinned) {
          Icon(
            painter = AppIcons.Fluent.Pin,
            contentDescription = null,
            tint = routine.contentColor,
            modifier = Modifier.size(18.dp),
          )
        }
      }
      if (!routine.description.isNullOrEmpty()) {
        Text(
          text = routine.description,
          color = routine.contentColor,
          style = MaterialTheme.typography.bodyMedium,
          maxLines = DESCRIPTION_MAX_LINES,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
      Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(BADGE_ROW_SPACING),
      ) {
        RoutineBadge(text = routine.stepCountLabel, contentColor = routine.contentColor)
        RoutineBadge(
          text = routine.durationLabel,
          contentColor = routine.contentColor,
          icon = AppIcons.Builder.Timer,
        )
        routine.scheduleRangeLabel?.let {
          RoutineBadge(text = it, contentColor = routine.contentColor)
        }
      }
      tagsContent?.let {
        Box(modifier = Modifier.padding(top = 8.dp)) { it() }
      }
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.End,
      ) {
        Button(
          onClick = onStartClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = routine.contentColor,
            contentColor = routine.backgroundColor,
          ),
          modifier = Modifier.testTag("routine_start_button_${routine.id}"),
        ) {
          Text(startButtonLabel)
        }
      }
    }
  }
}

@Composable
private fun RoutineBadge(
  text: String,
  contentColor: Color,
  icon: Painter? = null,
) {
  Surface(
    shape = MaterialTheme.shapes.extraSmall,
    color = contentColor.copy(alpha = 0.15f),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
      icon?.let {
        Icon(
          painter = it,
          contentDescription = null,
          tint = contentColor,
          modifier = Modifier.size(BADGE_ICON_SIZE),
        )
      }
      Text(
        text = text,
        color = contentColor,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = if (icon != null) 4.dp else 0.dp),
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun RoutineCardPreview() {
  AppTheme {
    RoutineCard(
      routine = UiRoutineListItem(
        id = "1",
        title = "Morning routine",
        description = "Meditate, journal, stretch",
        backgroundColor = Color(0xFF86E3CE),
        contentColor = Color.Black,
        isPinned = true,
        iconRes = null,
        stepCountLabel = "5 steps",
        durationLabel = "25m",
        scheduleRangeLabel = "07:00 - 07:45",
      ),
      startButtonLabel = "Start",
      onClick = {},
      onStartClick = {},
    )
  }
}
