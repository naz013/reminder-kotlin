package com.elementary.tasks.home

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.R
import com.elementary.tasks.eventaction.ResolvedEventAction
import com.elementary.tasks.home.scheduleview.ScheduleHomeViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

data class HomeScreenState(
  val greeting: String = "",
  val headerNavigationItems: List<HeaderNavigationItem> = emptyList(),
  val addMenuItems: List<ScheduleHomeViewModel.EventType> = emptyList(),
  val listState: ListState = ListState.Loading,
  val bannerState: BannerState? = null,
)

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
    val sections: List<TimeSection>,
  ) : ListState

  data object Empty : ListState
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
) {
  data class EventAction(
    val icon: Int,
    val value: ResolvedEventAction,
  ) {
    companion object IconRes {
      val MakeCall = R.drawable.ic_fluent_phone
      val SendSms = R.drawable.ic_fluent_send
      val SendEmail = R.drawable.ic_fluent_send
      val OpenLink = R.drawable.ic_fluent_globe
      val OpenApp = R.drawable.ic_fluent_open
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
