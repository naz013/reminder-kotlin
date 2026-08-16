package com.github.naz013.feature.reminder.apps

@Deprecated("After S")
data class SelectApplicationState(
  val listState: AppListState = AppListState.Loading,
  val searchQuery: String = "",
)

@Deprecated("After S")
sealed interface AppListState {
  data object Loading : AppListState

  data class Ready(
    val apps: List<UiApplicationList>,
  ) : AppListState

  data object Empty : AppListState
}
