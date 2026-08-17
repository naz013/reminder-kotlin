package com.github.naz013.feature.calendar.dayview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.feature.calendar.dayview.weekheader.WeekDay
import com.github.naz013.ui.agenda.AgendaMenuAction
import com.github.naz013.ui.agenda.BirthdayAgendaRow
import com.github.naz013.ui.agenda.ReminderAgendaRow
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.agenda.UiAgendaItem
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.domain.PublicHoliday
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import org.threeten.bp.LocalDate

private val DAY_CELL_NUMBER_SIZE = 32.dp
private val EVENT_DOT_SIZE = 5.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekViewScreen(
  state: WeekViewScreenState,
  initialPagerPosition: Int,
  pagerJumpRequest: Int?,
  onPagerJumpConsumed: () -> Unit,
  dateForPosition: (Int) -> LocalDate,
  onPageSettled: (Int) -> Unit,
  onDayClick: (WeekDay) -> Unit,
  refreshSignal: Int,
  loadDayEvents: suspend (LocalDate) -> List<UiAgendaItem>,
  loadDayHoliday: suspend (LocalDate) -> PublicHoliday?,
  onItemClick: (UiAgendaItem) -> Unit,
  onAgendaMenuAction: (UiAgendaItem, AgendaMenuAction) -> Unit,
  onAddReminderClick: () -> Unit,
  onAddBirthdayClick: () -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val pagerState = rememberPagerState(initialPage = initialPagerPosition) { Int.MAX_VALUE }

  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.settledPage }.collect { position -> onPageSettled(position) }
  }

  LaunchedEffect(pagerJumpRequest) {
    val target = pagerJumpRequest ?: return@LaunchedEffect
    pagerState.animateScrollToPage(target)
    onPagerJumpConsumed()
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      Column {
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
            AddMenuButton(
              onAddReminderClick = onAddReminderClick,
              onAddBirthdayClick = onAddBirthdayClick,
            )
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
        WeekDayHeaderRow(
          days = state.days,
          onDayClick = onDayClick,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }
    }
  ) { padding ->
    HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxSize().padding(padding),
    ) { position ->
      DayPage(
        date = dateForPosition(position),
        refreshSignal = refreshSignal,
        loadDayEvents = loadDayEvents,
        loadDayHoliday = loadDayHoliday,
        onItemClick = onItemClick,
        onAgendaMenuAction = onAgendaMenuAction,
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
          PopupMenuItem(id = 2, title = stringResource(R.string.add_birthday), iconRes = R.drawable.ic_fluent_food_cake),
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

@Composable
private fun WeekDayHeaderRow(
  days: List<WeekDay>,
  onDayClick: (WeekDay) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier) {
    days.forEach { day ->
      WeekDayCell(day = day, onClick = { onDayClick(day) }, modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun WeekDayCell(
  day: WeekDay,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .clip(MaterialTheme.shapes.medium)
        .clickable(enabled = !day.isSelected, onClick = onClick)
        .padding(vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = day.weekday.uppercase(),
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Box(
      modifier =
        Modifier
          .padding(top = 4.dp)
          .size(DAY_CELL_NUMBER_SIZE)
          .clip(CircleShape)
          .background(if (day.isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = day.date,
        style = MaterialTheme.typography.bodyMedium,
        color = if (day.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
      )
    }
    Box(
      modifier =
        Modifier
          .padding(top = 3.dp)
          .size(EVENT_DOT_SIZE)
          .clip(CircleShape)
          .background(if (day.hasEvents) MaterialTheme.colorScheme.primary else Color.Transparent),
    )
  }
}

@Composable
private fun DayPage(
  date: LocalDate,
  refreshSignal: Int,
  loadDayEvents: suspend (LocalDate) -> List<UiAgendaItem>,
  loadDayHoliday: suspend (LocalDate) -> PublicHoliday?,
  onItemClick: (UiAgendaItem) -> Unit,
  onAgendaMenuAction: (UiAgendaItem, AgendaMenuAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  var events by remember(date) { mutableStateOf<List<UiAgendaItem>?>(null) }
  var holiday by remember(date) { mutableStateOf<PublicHoliday?>(null) }

  LaunchedEffect(date, refreshSignal) {
    events = loadDayEvents(date)
  }

  // Loaded independently of events - a banner popping in a moment later is fine, it's a secondary
  // decoration and shouldn't block the day's agenda from rendering.
  LaunchedEffect(date, refreshSignal) {
    holiday = loadDayHoliday(date)
  }

  Column(modifier = modifier.fillMaxSize()) {
    holiday?.let { HolidayBanner(holiday = it) }

    when (val items = events) {
      null -> {
        Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      else -> {
        if (items.isEmpty()) {
          DayEmptyState(modifier = Modifier.weight(1f).fillMaxSize())
        } else {
          LazyColumn(
            modifier = Modifier.weight(1f).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(items, key = { "${it.id}_${it.dateTime}" }) { item ->
              when (item) {
                is UiAgendaReminder -> {
                  ReminderAgendaRow(
                    item = item,
                    onClick = { onItemClick(item) },
                    onMenuAction = { action -> onAgendaMenuAction(item, action) },
                    modifier = Modifier.animateItem(),
                  )
                }

                is UiAgendaBirthday -> {
                  BirthdayAgendaRow(
                    item = item,
                    onClick = { onItemClick(item) },
                    onMenuAction = { action -> onAgendaMenuAction(item, action) },
                    modifier = Modifier.animateItem(),
                  )
                }

                else -> Unit
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HolidayBanner(
  holiday: PublicHoliday,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.tertiaryContainer)
        .padding(horizontal = 16.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_fluent_globe),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onTertiaryContainer,
      modifier = Modifier.size(20.dp),
    )
    Text(
      text = if (holiday.type.isNotBlank()) "${holiday.screenText} – ${holiday.type}" else holiday.screenText,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onTertiaryContainer,
    )
  }
}

@Composable
private fun DayEmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_fluent_alert),
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    )
    Text(
      text = stringResource(R.string.no_events),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun WeekViewScreenPreview() {
  AppTheme {
    WeekViewScreen(
      state =
        WeekViewScreenState(
          title = "Today",
          days =
            (0..6).map {
              WeekDay(
                localDate = LocalDate.now().plusDays(it.toLong()),
                weekday = "Mon",
                date = "${it + 1}",
                isSelected = it == 0,
                hasEvents = it % 2 == 0,
              )
            },
        ),
      initialPagerPosition = 0,
      pagerJumpRequest = null,
      onPagerJumpConsumed = {},
      dateForPosition = { LocalDate.now() },
      onPageSettled = {},
      onDayClick = {},
      refreshSignal = 0,
      loadDayEvents = { emptyList() },
      loadDayHoliday = { null },
      onItemClick = {},
      onAgendaMenuAction = { _, _ -> },
      onAddReminderClick = {},
      onAddBirthdayClick = {},
      onBackClick = {},
    )
  }
}
