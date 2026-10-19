package com.github.naz013.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.home.scheduleview.ScheduleHomeViewModel
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppShapes
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.isDesktopScreen
import com.github.naz013.ui.common.compose.foundation.isTabletScreen
import com.github.naz013.ui.common.icon.DrawableCatalog
import kotlinx.coroutines.delay
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import kotlin.time.Duration.Companion.milliseconds

private const val TILE_STAGGER_DELAY_MS = 40L
private const val TILE_MAX_STAGGER_DELAY_MS = 240L

private const val LIST_ITEM_STAGGER_DELAY_MS = 30L
private const val LIST_ITEM_MAX_STAGGER_DELAY_MS = 180L

private const val TILE_ICON_TINT_ALPHA = 0.16f

@Composable
fun ChronologicalHomeScreen(
  modifier: Modifier = Modifier,
  state: HomeScreenState,
  onAddMenuItemClick: (ScheduleHomeViewModel.EventType) -> Unit,
  onSettingsClick: () -> Unit,
  onHeaderNavigationItemClick: (HeaderNavigationItem) -> Unit,
  onHeaderNavigationItemLongClick: () -> Unit,
  onEventClick: (HomeEvent) -> Unit,
  onEventActionClick: (HomeEvent.EventAction) -> Unit,
  onEarlierTodayClick: () -> Unit,
  onOverdueClick: () -> Unit,
) {
  // On Medium+ width, PersistentNavRailSceneDecoratorStrategy already wraps this screen in a
  // navigation rail carrying the same destinations - showing the grid/row too would be
  // redundant. Queried directly rather than passed in, so this stays in sync with the decorator
  // without either side needing to coordinate a flag.
  val showHeaderNavigation = !(isTabletScreen() || isDesktopScreen())
  val listState = rememberLazyListState()
  val isScrolled by remember {
    derivedStateOf {
      listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
    }
  }
  val headerContainerColor by animateColorAsState(
    targetValue = if (isScrolled) {
      MaterialTheme.colorScheme.surfaceContainer
    } else {
      MaterialTheme.colorScheme.background
    },
    label = "header_container_color",
  )

  Column(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.background(headerContainerColor),
    ) {
      Header(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        text = state.greeting,
        summary = state.daySummary,
        addMenuItems = state.addMenuItems,
        onAddMenuItemClick = onAddMenuItemClick,
        onSettingsClick = onSettingsClick,
        // On Medium+ width, Settings is a rail item (see appRailDestinations in AppNavGraph.kt) -
        // showing this button too would be a redundant second entry point.
        showSettingsButton = showHeaderNavigation,
      )
    }
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      state = listState,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (showHeaderNavigation) {
        item {
          HeaderNavigationCarousel(
            modifier = Modifier.padding(top = 4.dp),
            items = state.headerNavigationItems,
            onItemClick = onHeaderNavigationItemClick,
            onItemLongClick = onHeaderNavigationItemLongClick,
          )
        }
      }
      when (state.listState) {
        is ListState.Ready -> {
          scheduleItems(
            schedule = state.listState.schedule,
            onEarlierTodayClick = onEarlierTodayClick,
            onOverdueClick = onOverdueClick,
            onEventClick = onEventClick,
            onEventActionClick = onEventActionClick,
          )
        }

        is ListState.Empty -> {
          item {
            HomeEmptyState(
              modifier = Modifier.fillParentMaxSize(),
              title = stringResource(state.listState.kind.titleRes),
              message = state.listState.kind.messageRes?.let { stringResource(it) },
              addMenuItems = state.addMenuItems,
              onAddMenuItemClick = onAddMenuItemClick,
            )
          }
        }

        is ListState.Loading -> {}
      }
    }
  }
}

private fun LazyListScope.scheduleItems(
  schedule: TodaySchedule,
  onEarlierTodayClick: () -> Unit,
  onOverdueClick: () -> Unit,
  onEventClick: (HomeEvent) -> Unit,
  onEventActionClick: (HomeEvent.EventAction) -> Unit,
) {
  if (schedule.overdue.isNotEmpty()) {
    item(key = "overdue_header") {
      CollapsibleHeader(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = stringResource(R.string.home_overdue_title, schedule.overdueCount),
        color = MaterialTheme.colorScheme.error,
        isExpanded = schedule.isOverdueExpanded,
        onClick = onOverdueClick,
      )
    }
    if (schedule.isOverdueExpanded) {
      items(schedule.overdue, key = { "overdue_${it.id}" }) { event ->
        EventCard(
          modifier = Modifier.padding(horizontal = 16.dp),
          event = event,
          onEventClick = onEventClick,
          onEventActionClick = onEventActionClick,
        )
      }
    }
  }
  if (schedule.allDay.isNotEmpty()) {
    item(key = "all_day") {
      AllDayBand(
        events = schedule.allDay,
        onEventClick = onEventClick,
      )
    }
  }
  if (schedule.showEarlierHeader) {
    item(key = "earlier_header") {
      CollapsibleHeader(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = stringResource(R.string.home_earlier_today, schedule.earlierCount),
        isExpanded = schedule.isEarlierExpanded,
        onClick = onEarlierTodayClick,
      )
    }
    if (schedule.isEarlierExpanded) {
      itemsIndexed(
        items = schedule.earlierTimed,
        key = { _, section -> "earlier_${section.event.id}" },
      ) { index, section ->
        TimeSectionRow(
          modifier = Modifier.padding(horizontal = 16.dp),
          timeSection = section,
          index = index,
          onEventClick = onEventClick,
          onEventActionClick = onEventActionClick,
        )
      }
    }
  }
  if (schedule.showNowMarker) {
    item(key = "now_marker") {
      NowMarker(modifier = Modifier.padding(horizontal = 16.dp))
    }
  }
  if (schedule.upcomingTimed.isNotEmpty()) {
    item(key = "upcoming_title") {
      SectionTitle(text = stringResource(R.string.upcoming_events))
    }
    itemsIndexed(
      items = schedule.upcomingTimed,
      key = { _, section -> "upcoming_${section.event.id}" },
    ) { index, section ->
      TimeSectionRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        timeSection = section,
        index = index,
        onEventClick = onEventClick,
        onEventActionClick = onEventActionClick,
      )
    }
  }
  if (schedule.anytime.isNotEmpty()) {
    item(key = "anytime_title") {
      SectionTitle(text = stringResource(R.string.home_anytime_today))
    }
    items(schedule.anytime, key = { "anytime_${it.id}" }) { event ->
      EventCard(
        modifier = Modifier.padding(horizontal = 16.dp),
        event = event,
        onEventClick = onEventClick,
        onEventActionClick = onEventActionClick,
      )
    }
  }
}

@Composable
private fun SectionTitle(
  modifier: Modifier = Modifier,
  text: String,
) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.tertiary,
    modifier = modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
  )
}

@Composable
private fun AllDayBand(
  modifier: Modifier = Modifier,
  events: List<HomeEvent>,
  onEventClick: (HomeEvent) -> Unit,
) {
  Column(modifier = modifier) {
    SectionTitle(text = stringResource(R.string.all_day))
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
      items(events, key = { it.id }) { event ->
        AllDayChip(event = event, onClick = { onEventClick(event) })
      }
    }
  }
}

@Composable
private fun AllDayChip(
  modifier: Modifier = Modifier,
  event: HomeEvent,
  onClick: () -> Unit,
) {
  val containerColor = if (event.isSelected) {
    MaterialTheme.colorScheme.primaryContainer
  } else {
    MaterialTheme.colorScheme.tertiaryContainer
  }
  Surface(
    modifier = modifier
      .clip(AppShapes.tile)
      .clickable(role = Role.Button, onClick = onClick),
    shape = AppShapes.tile,
    color = containerColor,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(
        painter = painterResource(event.iconRes),
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = MaterialTheme.colorScheme.onTertiaryContainer,
      )
      event.text?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.labelLargeEmphasized,
          color = MaterialTheme.colorScheme.onTertiaryContainer,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun CollapsibleHeader(
  modifier: Modifier = Modifier,
  text: String,
  isExpanded: Boolean,
  onClick: () -> Unit,
  color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(AppShapes.tile)
      .toggleable(
        value = isExpanded,
        role = Role.Button,
        onValueChange = { onClick() },
      )
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.titleSmall,
      color = color,
    )
    Icon(
      painter = if (isExpanded) AppIcons.Fluent.ArrowUp else AppIcons.Fluent.ArrowDown,
      contentDescription = null,
      modifier = Modifier.size(18.dp),
      tint = color,
    )
  }
}

@Composable
private fun NowMarker(
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary),
    )
    Text(
      text = stringResource(R.string.home_now),
      style = MaterialTheme.typography.labelMediumEmphasized,
      color = MaterialTheme.colorScheme.primary,
    )
    HorizontalDivider(
      modifier = Modifier.weight(1f),
      color = MaterialTheme.colorScheme.primary,
    )
  }
}

@Composable
private fun Header(
  modifier: Modifier = Modifier,
  text: String,
  addMenuItems: List<ScheduleHomeViewModel.EventType>,
  onAddMenuItemClick: (ScheduleHomeViewModel.EventType) -> Unit,
  onSettingsClick: () -> Unit,
  summary: String = "",
  showSettingsButton: Boolean = true,
) {
  var addMenuExpanded by remember { mutableStateOf(false) }
  Greeting(
    modifier = modifier,
    greeting = text,
    summary = summary,
    trailingContent = {
      AddButton(
        items = addMenuItems,
        expanded = addMenuExpanded,
        onExpand = { addMenuExpanded = true },
        onDismiss = { addMenuExpanded = false },
        onItemClick = {
          addMenuExpanded = false
          onAddMenuItemClick(it)
        },
      )
      if (showSettingsButton) {
        MenuIconButton(
          modifier = Modifier.size(56.dp),
          icon = AppIcons.Fluent.Settings,
          onClick = onSettingsClick,
          contentDescription = stringResource(R.string.action_settings),
        )
      }
    },
  )
}

@Composable
private fun addPopupMenuItems(items: List<ScheduleHomeViewModel.EventType>): List<PopupMenuItem> =
  items.mapIndexed { index, eventType ->
    PopupMenuItem(
      id = index,
      title = stringResource(eventType.title),
      iconRes = when (eventType) {
        ScheduleHomeViewModel.EventType.QuickAdd -> DrawableCatalog.Fluent.Lightbulb
        ScheduleHomeViewModel.EventType.Reminder -> DrawableCatalog.Fluent.Alert
        ScheduleHomeViewModel.EventType.Birthday -> DrawableCatalog.Fluent.FoodCake
        ScheduleHomeViewModel.EventType.GoogleTask -> DrawableCatalog.Builder.GoogleTaskList
        ScheduleHomeViewModel.EventType.Note -> DrawableCatalog.Fluent.Note
        ScheduleHomeViewModel.EventType.Todo -> DrawableCatalog.Fluent.Cart
      },
    )
  }

@Composable
private fun HomeEmptyState(
  modifier: Modifier = Modifier,
  title: String,
  message: String?,
  addMenuItems: List<ScheduleHomeViewModel.EventType>,
  onAddMenuItemClick: (ScheduleHomeViewModel.EventType) -> Unit,
) {
  var addMenuExpanded by remember { mutableStateOf(false) }
  Column(
    modifier = modifier.padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = AppIcons.Fluent.CalendarAgenda,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      modifier = Modifier.padding(top = 12.dp),
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
    )
    message?.let {
      Text(
        modifier = Modifier.padding(top = 4.dp),
        text = it,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
    }
    Box(modifier = Modifier.padding(top = 16.dp)) {
      FilledTonalButton(onClick = { addMenuExpanded = true }) {
        Icon(
          painter = AppIcons.Fluent.Add,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(R.string.home_empty_today_action))
      }
      AppDropdownMenu(
        expanded = addMenuExpanded,
        onDismissRequest = { addMenuExpanded = false },
        items = addPopupMenuItems(addMenuItems),
        onItemClick = {
          addMenuExpanded = false
          onAddMenuItemClick(addMenuItems[it])
        },
      )
    }
  }
}

@Composable
private fun AddButton(
  modifier: Modifier = Modifier,
  items: List<ScheduleHomeViewModel.EventType>,
  expanded: Boolean,
  onExpand: () -> Unit,
  onDismiss: () -> Unit,
  onItemClick: (ScheduleHomeViewModel.EventType) -> Unit,
) {
  val menuItems = addPopupMenuItems(items)
  Box(modifier = modifier) {
    MenuIconButton(
      modifier = Modifier.size(56.dp),
      icon = AppIcons.Fluent.Add,
      onClick = onExpand,
      color = Color.Transparent,
      iconColor = MaterialTheme.colorScheme.primary,
      contentDescription = stringResource(R.string.acc_add),
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = onDismiss,
      items = menuItems,
      onItemClick = { onItemClick(items[it]) },
    )
  }
}

private val HEADER_NAV_ITEM_WIDTH = 128.dp
private val HEADER_NAV_ITEM_SPACING = 8.dp
private val HEADER_NAV_CAROUSEL_HEIGHT = 48.dp

// A single fixed-height row regardless of how many sections are enabled - the tile count used to
// drive a wrapping grid's height directly, which could push the event list below the fold once a
// user enabled more than a few sections. The trailing tile gets naturally cut off by the viewport
// (contentPadding keeps that cut consistent with the 16dp side margins elsewhere on Home) to
// signal there's more to scroll.
@Composable
private fun HeaderNavigationCarousel(
  modifier: Modifier = Modifier,
  items: List<HeaderNavigationItem>,
  onItemClick: (HeaderNavigationItem) -> Unit,
  onItemLongClick: () -> Unit,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(HEADER_NAV_CAROUSEL_HEIGHT)
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(HEADER_NAV_ITEM_SPACING),
  ) {
    items.forEachIndexed { index, item ->
      HeaderNavigationTile(
        modifier = Modifier
          .width(HEADER_NAV_ITEM_WIDTH)
          .fillMaxHeight(),
        item = item,
        index = index,
        onClick = { onItemClick(item) },
        onLongClick = onItemLongClick,
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeaderNavigationTile(
  modifier: Modifier = Modifier,
  item: HeaderNavigationItem,
  index: Int,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
) {
  var hasAnimated by rememberSaveable { mutableStateOf(false) }
  val visibleState = remember { MutableTransitionState(hasAnimated) }
  LaunchedEffect(Unit) {
    if (!hasAnimated) {
      delay((index * TILE_STAGGER_DELAY_MS).coerceAtMost(TILE_MAX_STAGGER_DELAY_MS).milliseconds)
      hasAnimated = true
    }
    visibleState.targetState = true
  }
  AnimatedVisibility(
    modifier = modifier,
    visibleState = visibleState,
    enter = fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
      scaleIn(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(), initialScale = 0.85f),
  ) {
    Surface(
      modifier = Modifier
        .clip(AppShapes.tile)
        .combinedClickable(onClick = onClick, onLongClick = onLongClick),
      shape = AppShapes.tile,
      color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(item.color.copy(alpha = TILE_ICON_TINT_ALPHA)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            painter = painterResource(item.iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = item.color,
          )
        }
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
          Text(
            text = stringResource(item.titleRes),
            style = MaterialTheme.typography.labelSmallEmphasized,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = item.subtitle,
            style = MaterialTheme.typography.titleMediumEmphasized,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
private fun TimeSectionRow(
  modifier: Modifier = Modifier,
  timeSection: TimeSection,
  index: Int,
  onEventClick: (HomeEvent) -> Unit,
  onEventActionClick: (HomeEvent.EventAction) -> Unit,
) {
  val visibleState = remember { MutableTransitionState(false) }
  LaunchedEffect(Unit) {
    delay((index * LIST_ITEM_STAGGER_DELAY_MS).coerceAtMost(LIST_ITEM_MAX_STAGGER_DELAY_MS).milliseconds)
    visibleState.targetState = true
  }
  AnimatedVisibility(
    modifier = modifier,
    visibleState = visibleState,
    enter = fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
      slideInVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) { fullHeight -> fullHeight / 6 },
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        modifier = Modifier
          .weight(1f)
          .padding(start = 0.dp, top = 16.dp),
        text = timeSection.time,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyMediumEmphasized,
      )
      EventCard(
        modifier = Modifier.weight(4f),
        event = timeSection.event,
        onEventClick = onEventClick,
        onEventActionClick = onEventActionClick,
      )
    }
  }
}

@Composable
private fun EventCard(
  modifier: Modifier = Modifier,
  event: HomeEvent,
  onEventClick: (HomeEvent) -> Unit,
  onEventActionClick: (HomeEvent.EventAction) -> Unit,
) {
  val containerColor: Color
  val onContainerColor: Color
  when {
    event.isSelected -> {
      containerColor = MaterialTheme.colorScheme.primaryContainer
      onContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    }

    event.isOverdue -> {
      containerColor = MaterialTheme.colorScheme.errorContainer
      onContainerColor = MaterialTheme.colorScheme.onErrorContainer
    }

    event.type == HomeEvent.EventType.Birthday -> {
      containerColor = MaterialTheme.colorScheme.tertiaryContainer
      onContainerColor = MaterialTheme.colorScheme.onTertiaryContainer
    }

    else -> {
      containerColor = CardDefaults.cardColors().containerColor
      onContainerColor = MaterialTheme.colorScheme.onSurface
    }
  }
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(AppShapes.card)
      .clickable(onClick = { onEventClick(event) }),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = if (event.isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
    elevation = CardDefaults.elevatedCardElevation(
      defaultElevation = 0.dp,
      pressedElevation = 2.dp,
    ),
  ) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
      event.accentColor?.let { accent ->
        Box(
          modifier = Modifier
            .width(6.dp)
            .fillMaxHeight()
            .background(accent),
        )
      }
      EventCardContent(
        modifier = Modifier.weight(1f),
        event = event,
        onContainerColor = onContainerColor,
        onEventActionClick = onEventActionClick,
      )
    }
  }
}

@Composable
private fun EventCardContent(
  modifier: Modifier = Modifier,
  event: HomeEvent,
  onContainerColor: Color,
  onEventActionClick: (HomeEvent.EventAction) -> Unit,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 16.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        event.text?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.bodyMediumEmphasized,
            color = onContainerColor,
          )
        }
        event.description?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = onContainerColor,
          )
        }
      }
      event.action?.let { action ->
        MenuIconButton(
          modifier = Modifier.size(48.dp),
          icon = painterResource(action.icon),
          iconColor = onContainerColor,
          contentDescription = when (action.value) {
            is ResolvedEventAction.MakeCall -> stringResource(R.string.make_call)
            is ResolvedEventAction.SendSms -> stringResource(R.string.send_sms)
            is ResolvedEventAction.SendEmail -> stringResource(R.string.action_send_email)
            is ResolvedEventAction.OpenApp -> stringResource(R.string.action_open)
            is ResolvedEventAction.OpenLink -> stringResource(R.string.open_link)
          },
          onClick = { onEventActionClick(event.action) },
        )
      }
    }

    event.groupName?.let {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = it,
        style = MaterialTheme.typography.bodySmallEmphasized,
        modifier = Modifier.fillMaxWidth(),
        color = onContainerColor,
      )
    }

    event.remaining?.let {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = it,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth(),
        color = onContainerColor,
      )
    }

    event.badge?.let {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = it,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.fillMaxWidth(),
        color = onContainerColor,
      )
    }
  }
}

@Composable
private fun Greeting(
  modifier: Modifier = Modifier,
  greeting: String,
  summary: String = "",
  trailingContent: @Composable RowScope.() -> Unit,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = greeting,
        style = MaterialTheme.typography.headlineMediumEmphasized,
        color = MaterialTheme.colorScheme.onBackground,
      )
      if (summary.isNotEmpty()) {
        Text(
          text = summary,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    Spacer(modifier = Modifier.width(16.dp))
    trailingContent()
  }
}

@Preview(showBackground = true)
@Composable
private fun TimeSectionRow_WithAction() {
  Box(
    modifier = Modifier.padding(16.dp),
  ) {
    TimeSectionRow(
      modifier = Modifier.fillMaxWidth(),
      timeSection = TimeSection(
        time = "12:00",
        event = HomeEvent(
          id = "",
          text = "Event text",
          description = "Event description",
          color = Color.Green,
          groupName = "Group",
          remaining = "10 hours",
          action = HomeEvent.EventAction(
            icon = HomeEvent.EventAction.MakeCall,
            value = ResolvedEventAction.MakeCall("+123456789"),
          ),
          date = LocalDate.of(2026, 6, 15),
          time = LocalTime.of(12, 0),
          isSelected = false,
          type = HomeEvent.EventType.Reminder,
        ),
      ),
      index = 0,
      onEventClick = {},
      onEventActionClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun EventCardPreview() {
  Box(
    modifier = Modifier.padding(16.dp),
  ) {
    EventCard(
      event = HomeEvent(
        id = "",
        text = "Event text",
        description = "Event description",
        color = Color.LightGray,
        groupName = "Group",
        remaining = "10 hours",
        action = null,
        date = LocalDate.of(2026, 6, 15),
        time = LocalTime.of(12, 0),
        isSelected = false,
        type = HomeEvent.EventType.Reminder,
      ),
      onEventClick = {},
      onEventActionClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun EventCardPreview_WithAction() {
  Box(
    modifier = Modifier.padding(16.dp),
  ) {
    EventCard(
      event = HomeEvent(
        id = "",
        text = "Event text",
        description = "Event description",
        color = Color.Green,
        groupName = "Group",
        remaining = "10 hours",
        action = HomeEvent.EventAction(
          icon = HomeEvent.EventAction.MakeCall,
          value = ResolvedEventAction.MakeCall("+123456789"),
        ),
        date = LocalDate.of(2026, 6, 15),
        time = LocalTime.of(12, 0),
        isSelected = false,
        type = HomeEvent.EventType.Reminder,
      ),
      onEventClick = {},
      onEventActionClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun EventCardPreview_Overdue() {
  Box(
    modifier = Modifier.padding(16.dp),
  ) {
    EventCard(
      event = HomeEvent(
        id = "",
        text = "Event text",
        description = "Event description",
        color = Color.Red,
        groupName = "Group",
        remaining = "Overdue",
        action = null,
        date = LocalDate.of(2026, 6, 15),
        time = LocalTime.of(12, 0),
        isSelected = false,
        type = HomeEvent.EventType.Reminder,
        isOverdue = true,
      ),
      onEventClick = {},
      onEventActionClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun EventCardPreview_Birthday() {
  Box(
    modifier = Modifier.padding(16.dp),
  ) {
    EventCard(
      event = HomeEvent(
        id = "",
        text = "Jane's birthday",
        description = null,
        color = Color.Magenta,
        groupName = null,
        remaining = "2 days",
        action = null,
        date = LocalDate.of(2026, 6, 15),
        time = LocalTime.of(12, 0),
        isSelected = false,
        type = HomeEvent.EventType.Birthday,
      ),
      onEventClick = {},
      onEventActionClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun NowMarkerPreview() {
  NowMarker(modifier = Modifier.padding(16.dp))
}

@Preview(showBackground = true)
@Composable
private fun CollapsibleHeaderPreview() {
  CollapsibleHeader(
    modifier = Modifier.padding(16.dp),
    text = "Earlier today (3)",
    isExpanded = false,
    onClick = {},
  )
}

@Preview(showBackground = true)
@Composable
private fun AllDayBandPreview() {
  AllDayBand(
    events = listOf(
      HomeEvent(
        id = "b1",
        text = "Jane's birthday",
        description = null,
        groupName = null,
        remaining = null,
        color = Color.Magenta,
        action = null,
        date = LocalDate.of(2026, 6, 15),
        time = LocalTime.MIDNIGHT,
        type = HomeEvent.EventType.Birthday,
      ),
    ),
    onEventClick = {},
  )
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
  Box(
    modifier = Modifier
      .size(width = 320.dp, height = 128.dp)
      .padding(16.dp),
  ) {
    Greeting(
      greeting = "Good Morning",
      trailingContent = { },
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview_WithIcon() {
  Box(
    modifier = Modifier
      .size(width = 320.dp, height = 128.dp)
      .padding(16.dp),
  ) {
    Greeting(
      greeting = "Good Morning",
      trailingContent = {
        Icon(
          painter = AppIcons.Fluent.Settings,
          contentDescription = null,
          tint = Color.Black,
        )
      },
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun HeaderPreview() {
  Box(
    modifier = Modifier
      .size(width = 320.dp, height = 128.dp)
      .padding(16.dp),
  ) {
    Header(
      text = "Good Morning",
      addMenuItems = listOf(ScheduleHomeViewModel.EventType.Reminder, ScheduleHomeViewModel.EventType.Birthday),
      onAddMenuItemClick = { },
      onSettingsClick = { },
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun HeaderWithPopupPreview() {
  val items = listOf(ScheduleHomeViewModel.EventType.Reminder, ScheduleHomeViewModel.EventType.Birthday)
  Box(
    modifier = Modifier
      .size(width = 320.dp, height = 220.dp)
      .padding(top = 16.dp, start = 16.dp, end = 16.dp),
  ) {
    Greeting(
      greeting = "Good Morning",
      trailingContent = {
        AddButton(
          items = items,
          expanded = true,
          onExpand = {},
          onDismiss = {},
          onItemClick = {},
        )
      },
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun HeaderNavigationCarouselPreview() {
  HeaderNavigationCarousel(
    items = listOf(
      HeaderNavigationItem(
        titleRes = R.string.calendar,
        iconRes = R.drawable.ic_fluent_calendar,
        color = Color(0xFF4CAF50),
        navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenNotes,
        subtitle = "12",
      ),
      HeaderNavigationItem(
        titleRes = R.string.agenda,
        iconRes = R.drawable.ic_fluent_timeline,
        color = Color(0xFF2196F3),
        navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenNotes,
        subtitle = "5",
      ),
      HeaderNavigationItem(
        titleRes = R.string.notes,
        iconRes = R.drawable.ic_fluent_note,
        color = Color(0xFFFFA726),
        navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenNotes,
        subtitle = "0",
      ),
      HeaderNavigationItem(
        titleRes = R.string.google_tasks,
        iconRes = R.drawable.ic_builder_google_task_list,
        color = Color(0xFFE53935),
        navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenNotes,
        subtitle = "3",
      ),
    ),
    onItemClick = {},
    onItemLongClick = {},
  )
}
