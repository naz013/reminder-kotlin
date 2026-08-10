package com.elementary.tasks.calendar.monthview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.elementary.tasks.calendar.monthview.monthgrid.MonthGridCell
import com.github.naz013.domain.PublicHoliday
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.CloudBubble
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import org.threeten.bp.LocalDate

private val DOT_SIZE = 5.dp
private const val MAX_DOTS = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
  state: CalendarScreenState,
  initialPagerPosition: Int,
  pagerJumpRequest: Int?,
  onPagerJumpConsumed: () -> Unit,
  monthForPosition: (Int) -> LocalDate,
  onPageSettled: (Int) -> Unit,
  buildGrid: (LocalDate) -> List<MonthGridCell>,
  refreshSignal: Int,
  loadMonthEvents: suspend (LocalDate) -> Map<LocalDate, List<Int>>,
  loadMonthHolidays: suspend (LocalDate) -> Map<LocalDate, PublicHoliday>,
  onDayClick: (LocalDate) -> Unit,
  onAddReminderClick: (LocalDate) -> Unit,
  onAddBirthdayClick: (LocalDate) -> Unit,
  onSettingsClick: () -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val pagerState = rememberPagerState(initialPage = initialPagerPosition) { Int.MAX_VALUE }

  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.settledPage }.collect { position -> onPageSettled(position) }
  }

  LaunchedEffect(pagerJumpRequest) {
    val target = pagerJumpRequest ?: return@LaunchedEffect
    pagerState.scrollToPage(target)
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
            OverflowMenuButton(onSettingsClick = onSettingsClick)
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
        WeekdayHeaderRow(
          labels = state.weekdayLabels,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }
    },
  ) { padding ->
    HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxSize().padding(padding),
    ) { position ->
      MonthPage(
        monthDate = monthForPosition(position),
        buildGrid = buildGrid,
        refreshSignal = refreshSignal,
        loadMonthEvents = loadMonthEvents,
        loadMonthHolidays = loadMonthHolidays,
        onDayClick = onDayClick,
        onAddReminderClick = onAddReminderClick,
        onAddBirthdayClick = onAddBirthdayClick,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}

@Composable
private fun WeekdayHeaderRow(
  labels: List<String>,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier) {
    labels.forEach { label ->
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun MonthPage(
  monthDate: LocalDate,
  buildGrid: (LocalDate) -> List<MonthGridCell>,
  refreshSignal: Int,
  loadMonthEvents: suspend (LocalDate) -> Map<LocalDate, List<Int>>,
  loadMonthHolidays: suspend (LocalDate) -> Map<LocalDate, PublicHoliday>,
  onDayClick: (LocalDate) -> Unit,
  onAddReminderClick: (LocalDate) -> Unit,
  onAddBirthdayClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val grid = remember(monthDate) { buildGrid(monthDate) }
  var eventsByDay by remember(monthDate) { mutableStateOf<Map<LocalDate, List<Int>>?>(null) }
  var holidaysByDay by remember(monthDate) { mutableStateOf<Map<LocalDate, PublicHoliday>>(emptyMap()) }

  LaunchedEffect(monthDate, refreshSignal) {
    eventsByDay = loadMonthEvents(monthDate)
  }

  // Loaded independently of eventsByDay - a holiday badge popping in a moment later than the
  // reminder/birthday dots is fine, it's a secondary decoration and shouldn't block the page.
  LaunchedEffect(monthDate, refreshSignal) {
    holidaysByDay = loadMonthHolidays(monthDate)
  }

  when (val events = eventsByDay) {
    null -> {
      Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    }

    else -> {
      Column(modifier = modifier) {
        grid.chunked(WEEK_LENGTH).forEach { week ->
          Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            week.forEach { cell ->
              MonthDayCell(
                cell = cell,
                dotColors = events[cell.date].orEmpty(),
                holiday = holidaysByDay[cell.date],
                onClick = onDayClick,
                onAddReminderClick = onAddReminderClick,
                onAddBirthdayClick = onAddBirthdayClick,
                modifier = Modifier.weight(1f).fillMaxSize(),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MonthDayCell(
  cell: MonthGridCell,
  dotColors: List<Int>,
  holiday: PublicHoliday?,
  onClick: (LocalDate) -> Unit,
  onAddReminderClick: (LocalDate) -> Unit,
  onAddBirthdayClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  Box(
    modifier =
      modifier.combinedClickable(
        onClick = { onClick(cell.date) },
        onLongClick = { expanded = true },
      ),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box(
        modifier =
          Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (cell.isToday) MaterialTheme.colorScheme.primary else Color.Transparent),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = cell.date.dayOfMonth.toString(),
          style = MaterialTheme.typography.bodyMedium,
          color =
            when {
              cell.isToday -> MaterialTheme.colorScheme.onPrimary
              cell.isCurrentMonth -> MaterialTheme.colorScheme.onSurface
              else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            },
        )
      }
      Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.padding(top = 3.dp),
      ) {
        dotColors.take(MAX_DOTS).forEach { color ->
          Box(
            modifier =
              Modifier
                .size(DOT_SIZE)
                .clip(CircleShape)
                .background(Color(color)),
          )
        }
        if (holiday != null) {
          Box(
            modifier =
              Modifier
                .size(DOT_SIZE)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary),
          )
        }
      }
    }

    if (expanded) {
      val bubbleContainerColor = MaterialTheme.colorScheme.surfaceContainer
      val bubbleContentColor = MaterialTheme.colorScheme.onSurface
      CloudBubble(
        onDismissRequest = { expanded = false },
        containerColor = bubbleContainerColor,
        contentColor = bubbleContentColor,
        modifier = Modifier.width(192.dp),
      ) {
        Column {
          AddEventRow(
            text = stringResource(R.string.add_reminder_menu),
            iconRes = R.drawable.ic_fluent_alert,
            contentColor = bubbleContentColor,
            onClick = { expanded = false; onAddReminderClick(cell.date) },
          )
          AddEventRow(
            text = stringResource(R.string.add_birthday),
            iconRes = R.drawable.ic_fluent_food_cake,
            contentColor = bubbleContentColor,
            onClick = { expanded = false; onAddBirthdayClick(cell.date) },
          )
        }
      }
    }
  }
}

@Composable
private fun AddEventRow(
  text: String,
  iconRes: Int,
  contentColor: Color,
  onClick: () -> Unit,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(vertical = 12.dp),
  ) {
    Icon(painter = painterResource(iconRes), contentDescription = null, tint = contentColor)
    Text(text = text, color = contentColor, style = MaterialTheme.typography.titleMedium)
  }
}

@Composable
private fun OverflowMenuButton(onSettingsClick: () -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    MenuIconButton(
      icon = painterResource(R.drawable.ic_fluent_more_vertical),
      contentDescription = stringResource(R.string.more_options),
      onClick = { expanded = true },
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items =
        listOf(
          PopupMenuItem(id = 0, title = stringResource(R.string.action_settings), iconRes = R.drawable.ic_fluent_settings),
        ),
      onItemClick = { onSettingsClick() },
    )
  }
}

private const val WEEK_LENGTH = 7

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
  AppTheme {
    CalendarScreen(
      state =
        CalendarScreenState(
          title = "July 2026",
          weekdayLabels = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"),
        ),
      initialPagerPosition = 0,
      pagerJumpRequest = null,
      onPagerJumpConsumed = {},
      monthForPosition = { LocalDate.now() },
      onPageSettled = {},
      buildGrid = { monthDate ->
        val first = monthDate.withDayOfMonth(1)
        (0 until 42).map { offset ->
          val date = first.minusDays(first.dayOfWeek.value.toLong() - 1).plusDays(offset.toLong())
          MonthGridCell(date = date, isCurrentMonth = date.monthValue == monthDate.monthValue, isToday = date == LocalDate.now())
        }
      },
      refreshSignal = 0,
      loadMonthEvents = { emptyMap() },
      loadMonthHolidays = { emptyMap() },
      onDayClick = {},
      onAddReminderClick = {},
      onAddBirthdayClick = {},
      onSettingsClick = {},
      onBackClick = {},
    )
  }
}
