package com.github.naz013.feature.habit.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons

private val ICON_SIZE = 20.dp
private val CHECK_BUTTON_SIZE = 48.dp

@Composable
internal fun HabitCard(
  modifier: Modifier = Modifier,
  habit: UiHabitListItem,
  onClick: () -> Unit,
  onCheckInClick: () -> Unit,
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.medium)
      .clickable(onClick = onClick)
      .testTag("habit_card_${habit.id}"),
    colors = CardDefaults.cardColors(containerColor = habit.backgroundColor),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      habit.iconRes?.let {
        Icon(
          painter = painterResource(it),
          contentDescription = null,
          tint = habit.contentColor,
          modifier = Modifier.size(ICON_SIZE),
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = habit.name,
          color = habit.contentColor,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        habit.weekProgressLabel?.let {
          Text(
            text = it,
            color = habit.contentColor,
            style = MaterialTheme.typography.bodySmall,
          )
        }
        Row(
          modifier = Modifier.padding(top = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            painter = AppIcons.Fluent.Star,
            contentDescription = habit.streakDescription,
            tint = habit.contentColor,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = habit.streakLabel,
            color = habit.contentColor,
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
      Text(
        text = habit.todayProgressLabel,
        color = habit.contentColor,
        style = MaterialTheme.typography.titleMedium,
      )
      IconButton(
        onClick = onCheckInClick,
        modifier = Modifier.size(CHECK_BUTTON_SIZE).testTag("habit_check_in_${habit.id}"),
        colors = IconButtonDefaults.iconButtonColors(
          containerColor = if (habit.isTargetReached) habit.contentColor else habit.backgroundColor,
          contentColor = if (habit.isTargetReached) habit.backgroundColor else habit.contentColor,
        ),
      ) {
        Icon(
          painter = AppIcons.Fluent.CheckmarkCircle,
          contentDescription = stringResource(R.string.habit_check_in),
        )
      }
    }
  }
}
