package com.github.naz013.feature.calendar.timeline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.R
import com.github.naz013.feature.calendar.CalendarModeToggleButton
import com.github.naz013.feature.calendar.CalendarViewMode
import com.github.naz013.ui.agenda.UiAgendaItem
import com.github.naz013.domain.PublicHoliday
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import org.threeten.bp.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
  state: TimelineScreenState,
  currentMode: CalendarViewMode,
  onModeSelected: (CalendarViewMode) -> Unit,
  initialPagerPosition: Int,
  pagerJumpRequest: Int?,
  onPagerJumpConsumed: () -> Unit,
  windowStartForPosition: (Int) -> LocalDate,
  daysForWindow: (LocalDate) -> List<TimelineDay>,
  onPageSettled: (Int) -> Unit,
  refreshSignal: Int,
  loadWindowEvents: suspend (LocalDate) -> Map<LocalDate, List<UiAgendaItem>>,
  loadWindowHolidays: suspend (LocalDate) -> Map<LocalDate, PublicHoliday>,
  onItemClick: (UiAgendaItem) -> Unit,
  onDayHeaderClick: (LocalDate) -> Unit,
  onAddReminderClick: () -> Unit,
  onAddBirthdayClick: () -> Unit,
  onBackClick: () -> Unit,
  initialScrollOffset: Int,
  onScrollOffsetChanged: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(state.title) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        actions = {
          CalendarModeToggleButton(currentMode = currentMode, onModeSelected = onModeSelected)
          AddMenuButton(
            onAddReminderClick = onAddReminderClick,
            onAddBirthdayClick = onAddBirthdayClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
      )
    },
  ) { padding ->
    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
      TimelinePager(
        initialPagerPosition = initialPagerPosition,
        pagerJumpRequest = pagerJumpRequest,
        onPagerJumpConsumed = onPagerJumpConsumed,
        windowStartForPosition = windowStartForPosition,
        daysForWindow = daysForWindow,
        hourLabels = state.hourLabels,
        onPageSettled = onPageSettled,
        refreshSignal = refreshSignal,
        loadWindowEvents = loadWindowEvents,
        loadWindowHolidays = loadWindowHolidays,
        onItemClick = onItemClick,
        onDayHeaderClick = onDayHeaderClick,
        initialScrollOffset = initialScrollOffset,
        onScrollOffsetChanged = onScrollOffsetChanged,
      )
    }
  }
}

@Composable
private fun AddMenuButton(
  onAddReminderClick: () -> Unit,
  onAddBirthdayClick: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    MenuIconButton(
      icon = AppIcons.Fluent.Add,
      contentDescription = stringResource(R.string.acc_add),
      onClick = { expanded = true },
      iconColor = MaterialTheme.colorScheme.primary,
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items =
        listOf(
          PopupMenuItem(id = 0, title = stringResource(R.string.new_reminder), iconRes = R.drawable.ic_fluent_alert),
          PopupMenuItem(id = 1, title = stringResource(R.string.add_birthday), iconRes = R.drawable.ic_fluent_food_cake),
        ),
      onItemClick = { id ->
        when (id) {
          0 -> onAddReminderClick()
          1 -> onAddBirthdayClick()
        }
      },
    )
  }
}
