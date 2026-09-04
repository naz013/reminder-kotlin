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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import com.github.naz013.ui.agenda.UiAgendaGoogleCalendarEvent
import com.github.naz013.ui.agenda.UiAgendaItem
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.CloudBubble
import com.github.naz013.domain.PublicHoliday
import kotlinx.coroutines.delay
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

private val HOUR_HEIGHT = 56.dp
private val AXIS_WIDTH = 52.dp
private val EVENT_BLOCK_MIN_HEIGHT = 26.dp
private val NOW_LINE_DOT_RADIUS = 4.dp
private val NOW_LINE_STROKE_WIDTH = 2.dp
private const val INITIAL_SCROLL_LEAD_HOURS = 2f
private const val NOW_LINE_REFRESH_INTERVAL_MS = 30_000L

/**
 * Vertical hour-timeline pager shared by the 3-day and 7-day calendar modes. Each page shows one
 * window of day columns laid out against a shared 24-hour axis; the hour axis and all day columns
 * live in one vertical scroll container so they always scroll together, and that scroll state is
 * hoisted above the [HorizontalPager] so the vertical position is preserved while swiping between
 * windows. Overlapping events within a day are split into side-by-side lanes by [layoutDayEvents].
 */
@Composable
internal fun TimelinePager(
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
  initialScrollOffset: Int,
  onScrollOffsetChanged: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val pagerState = rememberPagerState(initialPage = initialPagerPosition) { Int.MAX_VALUE }
  // Hoisted above the pager so swiping between windows keeps the same vertical scroll offset.
  // Seeded from initialScrollOffset (persisted in the ViewModel) rather than always starting at
  // 0, since navigating away to a reminder/birthday preview and back tears down and recreates
  // this whole composable - a plain remember here would forget the scroll position every time.
  val scrollState = rememberScrollState(initial = initialScrollOffset.coerceAtLeast(0))
  // Guards the scroll-to-now effect below: only run it when there's no persisted offset to
  // restore (i.e. this mode has never been scrolled before), and even then only once - on
  // whichever page is initially shown, once its content has actually loaded (scrollState has
  // nothing to scroll to before then).
  val hasScrolledToNow = remember { mutableStateOf(initialScrollOffset != TimelineViewModel.NO_SCROLL_OFFSET) }

  LaunchedEffect(scrollState) {
    snapshotFlow { scrollState.value }.collect { onScrollOffsetChanged(it) }
  }

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
      isInitialPage = position == initialPagerPosition,
      hasScrolledToNow = hasScrolledToNow,
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
  isInitialPage: Boolean,
  hasScrolledToNow: MutableState<Boolean>,
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
    // A single day is already named in the app bar title, so the weekday/date header (which
    // otherwise also acts as the "jump to this day" control for multi-day windows) is redundant.
    if (days.size > 1) {
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
    }
    TimelineHolidayRow(days = days, holidaysByDay = holidaysByDay, modifier = Modifier.fillMaxWidth())
    HorizontalDivider()

    // The grid itself (hour axis, gridlines, the "now" line) doesn't depend on events - render it
    // immediately rather than hiding it behind a full-screen spinner, since a heavily-populated
    // window (thousands of occurrences) can take a moment to query. Only the event blocks
    // themselves wait for eventsByDay, appearing once loaded; a thin bar above the grid is the
    // only sign anything is still in flight.
    if (eventsByDay == null) {
      LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }

    if (isInitialPage && !hasScrolledToNow.value) {
      val density = LocalDensity.current
      LaunchedEffect(Unit) {
        val now = LocalTime.now()
        val hourHeightPx = with(density) { HOUR_HEIGHT.toPx() }
        val target =
          (((now.hour + now.minute / 60f) - INITIAL_SCROLL_LEAD_HOURS) * hourHeightPx).toInt().coerceAtLeast(0)
        scrollState.scrollTo(target)
        hasScrolledToNow.value = true
      }
    }
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
          items = eventsByDay?.get(day.date).orEmpty(),
          isToday = day.isToday,
          onItemClick = onItemClick,
          modifier = Modifier.weight(1f),
        )
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
  var expanded by remember { mutableStateOf(false) }
  val containerColor = MaterialTheme.colorScheme.tertiaryContainer
  val contentColor = MaterialTheme.colorScheme.onTertiaryContainer

  Box(modifier = modifier) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(containerColor)
          .clickable { expanded = true }
          .padding(horizontal = 4.dp, vertical = 3.dp),
      horizontalArrangement = Arrangement.spacedBy(3.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_fluent_globe),
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(12.dp),
      )
      Text(
        text = holiday.screenText,
        style = MaterialTheme.typography.labelSmall,
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    if (expanded) {
      CloudBubble(
        onDismissRequest = { expanded = false },
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = Modifier.widthIn(max = 260.dp),
      ) {
        Text(
          text = if (holiday.type.isNotBlank()) "${holiday.screenText} – ${holiday.type}" else holiday.screenText,
          style = MaterialTheme.typography.bodyMedium,
          color = contentColor,
        )
      }
    }
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
  isToday: Boolean,
  onItemClick: (UiAgendaItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  val positioned = remember(items) { layoutDayEvents(items, MINUTES_IN_HOUR) }
  val density = LocalDensity.current
  val gridColor = MaterialTheme.colorScheme.outlineVariant
  val nowColor = MaterialTheme.colorScheme.primary

  // Only today's column ever ticks - there's at most one such column mounted per window.
  var now by remember { mutableStateOf(LocalTime.now()) }
  if (isToday) {
    LaunchedEffect(Unit) {
      while (true) {
        now = LocalTime.now()
        delay(NOW_LINE_REFRESH_INTERVAL_MS)
      }
    }
  }

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
          if (isToday) {
            val nowY = hourHeightPx * (now.hour + now.minute / 60f + now.second / 3600f)
            drawLine(
              color = nowColor,
              start = Offset(0f, nowY),
              end = Offset(size.width, nowY),
              strokeWidth = with(density) { NOW_LINE_STROKE_WIDTH.toPx() },
            )
            drawCircle(
              color = nowColor,
              radius = with(density) { NOW_LINE_DOT_RADIUS.toPx() },
              center = Offset(0f, nowY),
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
        onItemClick = onItemClick,
      )
    }
  }
}

/**
 * Tapping the block itself opens a [CloudBubble] (same pattern as [HolidayChip]) showing the
 * event's full, untruncated text in the block's own colors; tapping *that* bubble is what
 * actually opens the event's details, via [onItemClick].
 */
@Composable
private fun TimelineEventBlock(
  item: UiAgendaItem,
  width: Dp,
  xOffset: Dp,
  topOffset: Dp,
  onItemClick: (UiAgendaItem) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val container: Color
  val content: Color
  when (item) {
    is UiAgendaBirthday -> {
      container = Color(item.color)
      content = Color(item.contrastColor)
    }
    is UiAgendaGoogleCalendarEvent -> {
      container = MaterialTheme.colorScheme.tertiaryContainer
      content = MaterialTheme.colorScheme.onTertiaryContainer
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
      is UiAgendaGoogleCalendarEvent -> item.title
      else -> ""
    }
  val subtitle =
    when (item) {
      is UiAgendaReminder -> item.secondaryText?.text
      is UiAgendaBirthday -> item.ageFormatted
      is UiAgendaGoogleCalendarEvent -> item.calendarName.ifEmpty { null }
      else -> null
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
        .clickable { expanded = true }
        .padding(horizontal = 6.dp, vertical = 3.dp),
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall,
      color = content,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )

    if (expanded) {
      CloudBubble(
        onDismissRequest = { expanded = false },
        containerColor = container,
        contentColor = content,
        modifier = Modifier.widthIn(min = 160.dp, max = 260.dp),
      ) {
        Column(
          modifier =
            Modifier
              .clip(MaterialTheme.shapes.small)
              .clickable {
                expanded = false
                onItemClick(item)
              },
        ) {
          Text(text = title, style = MaterialTheme.typography.titleMedium, color = content)
          if (!subtitle.isNullOrBlank()) {
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = content.copy(alpha = 0.85f),
              modifier = Modifier.padding(top = 2.dp),
            )
          }
        }
      }
    }
  }
}
