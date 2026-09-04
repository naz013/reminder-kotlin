package com.github.naz013.feature.calendar.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.navigation.detailScreenContentWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoogleCalendarEventPreviewScreen(
  modifier: Modifier = Modifier,
  state: GoogleCalendarEventPreviewState,
  renderAsDetailPane: Boolean = false,
  onBackClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onDeleteLocalOnly: () -> Unit,
  onDeleteFromDeviceCalendarToo: () -> Unit,
  onDeleteOptionsDismiss: () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.google_calendar_event)) },
        navigationIcon = {
          MenuIconButton(
            icon = if (renderAsDetailPane) AppIcons.Fluent.Dismiss else AppIcons.Builder.ArrowLeft,
            contentDescription = if (renderAsDetailPane) stringResource(R.string.acc_close) else null,
            onClick = onBackClick,
          )
        },
        actions = {
          MenuIconButton(
            icon = painterResource(R.drawable.ic_fluent_delete),
            contentDescription = stringResource(R.string.delete),
            onClick = onDeleteClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
  ) { padding ->
    if (state.isLoading) {
      Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
      return@Scaffold
    }

    Box(
      modifier = Modifier.fillMaxSize().padding(padding),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(modifier = Modifier.detailScreenContentWidth()) {
        Card(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(text = state.title, style = MaterialTheme.typography.titleLarge)
            val dateTimeText =
              if (state.allDay) {
                stringResource(R.string.all_day) + " · " + state.dateTimeFormatted
              } else {
                state.dateTimeFormatted
              }
            Text(
              text = dateTimeText,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(top = 4.dp),
            )
            if (state.calendarName.isNotEmpty()) {
              Text(
                text = state.calendarName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
              )
            }
            if (state.description.isNotEmpty()) {
              Text(
                text = state.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp),
              )
            }
          }
        }
      }
    }
  }

  if (state.showDeleteOptions) {
    GoogleCalendarEventDeleteDialog(
      onDeleteLocalOnly = onDeleteLocalOnly,
      onDeleteFromDeviceCalendarToo = onDeleteFromDeviceCalendarToo,
      onDismiss = onDeleteOptionsDismiss,
    )
  }
}
