package com.github.naz013.feature.home

import androidx.compose.ui.graphics.Color
import com.github.naz013.feature.home.scheduleview.ScheduleHomeViewModel
import com.github.naz013.ui.common.icon.DrawableCatalog
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

data class HomeScreenState(
  val greeting: String = "",
  val headerNavigationItems: List<HeaderNavigationItem> = emptyList(),
  val addMenuItems: List<ScheduleHomeViewModel.EventType> = emptyList(),
  val listState: ListState = ListState.Loading,
  val bannerState: BannerState? = null,
)

fun HomeScreenState.withSelectedEvent(selectedEventId: String?): HomeScreenState {
  val ready = listState as? ListState.Ready ?: return this
  val schedule = ready.schedule
  fun HomeEvent.select() = copy(isSelected = id == selectedEventId)
  fun List<TimeSection>.select() = map { it.copy(event = it.event.select()) }
  return copy(
    listState = ready.copy(
      schedule = schedule.copy(
        earlierTimed = schedule.earlierTimed.select(),
        upcomingTimed = schedule.upcomingTimed.select(),
        allDay = schedule.allDay.map { it.select() },
        anytime = schedule.anytime.map { it.select() },
      ),
    ),
  )
}

data class HeaderNavigationItem(
  val titleRes: Int,
  val iconRes: Int,
  val color: Color,
  val navigationEvent: ScheduleHomeViewModel.ViewModelEvent,
  val subtitle: String,
)

sealed interface ListState {
  data object Loading : ListState

  data class Ready(
    val schedule: TodaySchedule = TodaySchedule(),
  ) : ListState

  data object Empty : ListState
}

data class TodaySchedule(
  val earlierTimed: List<TimeSection> = emptyList(),
  val upcomingTimed: List<TimeSection> = emptyList(),
  val allDay: List<HomeEvent> = emptyList(),
  val anytime: List<HomeEvent> = emptyList(),
  val isEarlierExpanded: Boolean = false,
) {
  val earlierCount: Int get() = earlierTimed.size
  val showNowMarker: Boolean get() = earlierTimed.isNotEmpty() || upcomingTimed.isNotEmpty()
  val showEarlierHeader: Boolean get() = earlierTimed.isNotEmpty()
}

data class TimeSection(
  val time: String,
  val event: HomeEvent,
)

data class HomeEvent(
  val id: String,
  val text: String?,
  val description: String?,
  val groupName: String?,
  val remaining: String?,
  val color: Color,
  val action: EventAction?,
  val date: LocalDate,
  val time: LocalTime,
  val type: EventType,
  val isSelected: Boolean = false,
  val isOverdue: Boolean = false,
) {
  data class EventAction(
    val icon: Int,
    val value: ResolvedEventAction,
  ) {
    companion object IconRes {
      val MakeCall = DrawableCatalog.Fluent.Phone
      val SendSms = DrawableCatalog.Fluent.Send
      val SendEmail = DrawableCatalog.Fluent.Send
      val OpenLink = DrawableCatalog.Fluent.Globe
      val OpenApp = DrawableCatalog.Fluent.Open
    }
  }

  enum class EventType {
    Reminder,
    Birthday,
  }
}

sealed interface BannerState {
  data object Privacy : BannerState

  data object Login : BannerState

  data object WhatsNew : BannerState
}
