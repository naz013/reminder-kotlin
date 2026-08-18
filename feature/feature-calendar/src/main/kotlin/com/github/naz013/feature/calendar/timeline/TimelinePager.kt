package com.github.naz013.feature.calendar.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.agenda.UiAgendaItem
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.ui.common.R
import com.github.naz013.domain.PublicHoliday
import org.threeten.bp.LocalDate

private val HOUR_HEIGHT = 56.dp
private val AXIS_WIDTH = 52.dp
private val EVENT_BLOCK_MIN_HEIGHT = 26.dp

/**
 * Vertical hour-timeline pager shared by the 3-day and 7-day calendar modes. Each page shows one
 * window of day columns laid out against a shared 24-hour axis; the hour axis and all day columns
 * live in one vertical scroll container so they always scroll together, and that scroll state is
 * hoisted above the [HorizontalPager] so the vertical position is preserved while swiping between
 * windows. Overlapping events within a day are split into side-by-side lanes by [layoutDayEvents].
 */
@Composable
fun TimelinePager(
  initialPagerPosition: Int,
  pagerJumpRequest: Int?,
  onPagerJumpConsumed: () -> Unit,
  windowStartForPosition: (Int) -> LocalDate,
  daysForWindow: (LocalDate) -> List<TimelineDay>,
  hourLabels: List<String>,
  onPageSettled: (Int) -> Unit,
  refreshSignal: Int,
  loadWindowEvents: suspend (LocalDate) -> Map<LocalDate, List<UiAgendaItem>>,
  loadWindowHolidays: suspend (LocalDate) -> Map<LocalDate, PublicHoliday>,
  onItemClick: (UiAgendaItem) -> Unit,
  onDayHeaderClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val pagerState = rememberPagerState(initialPage = initialPagerPosition) { Int.MAX_VALUE }
  // Hoisted above the pager so swiping between windows keeps the same vertical scroll offset.
  val scrollState = rememberScrollState()

  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.settledPage }.collect { position -> onPageSettled(position) }
  }

  LaunchedEffect(pagerJumpRequest) {
    val target = pagerJumpRequest ?: return@LaunchedEffect
    pagerState.scrollToPage(target)
    onPagerJumpConsumed()
  }

  HorizontalPager(
    state = pagerState,
    modifier = modifier.fillMaxSize(),
  ) { position ->
    TimelinePage(
      windowStart = windowStartForPosition(position),
      daysForWindow = daysForWindow,
      hourLabels = hourLabels,
      scrollState = scrollState,
      refreshSignal = refreshSignal,
      loadWindowEvents = loadWindowEvents,
      loadWindowHolidays = loadWindowHolidays,
      onItemClick = onItemClick,
      onDayHeaderClick = onDayHeaderClick,
    )
  }
}

@Composable
private fun TimelinePage(
  windowStart: LocalDate,
  daysForWindow: (LocalDate) -> List<TimelineDay>,
  hourLabels: List<String>,
  scrollState: androidx.compose.foundation.ScrollState,
  refreshSignal: Int,
  loadWindowEvents: suspend (LocalDate) -> Map<LocalDate, List<UiAgendaItem>>,
  loadWindowHolidays: suspend (LocalDate) -> Map<LocalDate, PublicHoliday>,
  onItemClick: (UiAgendaItem) -> Unit,
  onDayHeaderClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val days = remember(windowStart) { daysForWindow(windowStart) }
  var eventsByDay by remember(windowStart) { mutableStateOf<Map<LocalDate, List<UiAgendaItem>>?>(null) }
  var holidaysByDay by remember(windowStart) { mutableStateOf<Map<LocalDate, PublicHoliday>>(emptyMap()) }

  LaunchedEffect(windowStart, refreshSignal) {
    eventsByDay = loadWindowEvents(windowStart)
  }

  // Loaded independently of events - a holiday marker popping in a moment later is fine.
  LaunchedEffect(windowStart, refreshSignal) {
    holidaysByDay = loadWindowHolidays(windowStart)
  }

  Column(modifier = modifier.fillMaxSize()) {
    Row(modifier = Modifier.fillMaxWidth()) {
      Spacer(modifier = Modifier.width(AXIS_WIDTH))
      days.forEach { day ->
        TimelineDayHeader(
          day = day,
          onClick = { onDayHeaderClick(day.date) },
          modifier = Modifier.weight(1f),
        )
      }
    }
    TimelineHolidayRow(days = days, holidaysByDay = holidaysByDay, modifier = Modifier.fillMaxWidth())
    HorizontalDivider()

    when (val events = eventsByDay) {
      null -> {
        Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      else -> {
        Row(
          modifier =
            Modifier
              .weight(1f)
              .fillMaxWidth()
              .verticalScroll(scrollState),
        ) {
          HourAxis(hourLabels = hourLabels, modifier = Modifier.width(AXIS_WIDTH))
          days.forEach { day ->
            TimelineDayColumn(
              items = events[day.date].orEmpty(),
              onItemClick = onItemClick,
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TimelineDayHeader(
  day: TimelineDay,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onClick)
        .padding(vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = day.weekdayLabel.uppercase(),
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Box(
      modifier =
        Modifier
          .padding(top = 4.dp)
          .size(28.dp)
          .clip(CircleShape)
          .background(if (day.isToday) MaterialTheme.colorScheme.primary else Color.Transparent),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = day.dayLabel,
        style = MaterialTheme.typography.bodyMedium,
        color = if (day.isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

/**
 * One row, one cell per visible day: each day that has a public holiday gets a compact chip
 * (icon + ellipsized name) in its own column's width - the multi-day equivalent of the full-width
 * holiday banner the single-day timeline shows. Renders nothing at all when no visible day has a
 * holiday, so it doesn't reserve empty vertical space in the common case.
 */
@Composable
private fun TimelineHolidayRow(
  days: List<TimelineDay>,
  holidaysByDay: Map<LocalDate, PublicHoliday>,
  modifier: Modifier = Modifier,
) {
  if (holidaysByDay.isEmpty()) return
  Row(modifier = modifier) {
    Spacer(modifier = Modifier.width(AXIS_WIDTH))
    days.forEach { day ->
      Box(modifier = Modifier.weight(1f).padding(horizontal = 2.dp, vertical = 2.dp)) {
        holidaysByDay[day.date]?.let { holiday -> HolidayChip(holiday = holiday) }
      }
    }
  }
}

@Composable
private fun HolidayChip(
  holiday: PublicHoliday,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(6.dp))
        .background(MaterialTheme.colorScheme.tertiaryContainer)
        .padding(horizontal = 4.dp, vertical = 3.dp),
    horizontalArrangement = Arrangement.spacedBy(3.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_fluent_globe),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onTertiaryContainer,
      modifier = Modifier.size(12.dp),
    )
    Text(
      text = holiday.screenText,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onTertiaryContainer,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun HourAxis(
  hourLabels: List<String>,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.height(HOUR_HEIGHT * HOURS_IN_DAY)) {
    hourLabels.forEachIndexed { hour, label ->
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.End,
        modifier =
          Modifier
            .offset(y = HOUR_HEIGHT * hour)
            .width(AXIS_WIDTH)
            .padding(end = 6.dp, top = 2.dp),
      )
    }
  }
}

@Composable
private fun TimelineDayColumn(
  items: List<UiAgendaItem>,
  onItemClick: (UiAgendaItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  val positioned = remember(items) { layoutDayEvents(items, MINUTES_IN_HOUR) }
  val density = LocalDensity.current
  val gridColor = MaterialTheme.colorScheme.outlineVariant

  BoxWithConstraints(
    modifier =
      modifier
        .height(HOUR_HEIGHT * HOURS_IN_DAY)
        .drawBehind {
          val hourHeightPx = with(density) { HOUR_HEIGHT.toPx() }
          for (hour in 0..HOURS_IN_DAY) {
            val y = hourHeightPx * hour
            drawLine(
              color = gridColor,
              start = Offset(0f, y),
              end = Offset(size.width, y),
              strokeWidth = 1f,
            )
          }
        },
  ) {
    positioned.forEach { positionedEvent ->
      val eventLaneWidth = maxWidth / positionedEvent.laneCount
      TimelineEventBlock(
        item = positionedEvent.item,
        width = eventLaneWidth,
        xOffset = eventLaneWidth * positionedEvent.lane,
        topOffset = HOUR_HEIGHT * (positionedEvent.startMinutes / MINUTES_IN_HOUR.toFloat()),
        onClick = { onItemClick(positionedEvent.item) },
      )
    }
  }
}

@Composable
private fun TimelineEventBlock(
  item: UiAgendaItem,
  width: Dp,
  xOffset: Dp,
  topOffset: Dp,
  onClick: () -> Unit,
) {
  val container: Color
  val content: Color
  when (item) {
    is UiAgendaBirthday -> {
      container = Color(item.color)
      content = Color(item.contrastColor)
    }
    else -> {
      container = MaterialTheme.colorScheme.primaryContainer
      content = MaterialTheme.colorScheme.onPrimaryContainer
    }
  }
  val title =
    when (item) {
      is UiAgendaReminder -> item.mainText.text
      is UiAgendaBirthday -> item.name
      else -> ""
    }

  Box(
    modifier =
      Modifier
        .offset(x = xOffset, y = topOffset)
        .width(width)
        .height(HOUR_HEIGHT)
        .heightIn(min = EVENT_BLOCK_MIN_HEIGHT)
        .padding(horizontal = 1.dp, vertical = 1.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(container)
        .clickable(onClick = onClick)
        .padding(horizontal = 6.dp, vertical = 3.dp),
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall,
      color = content,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
