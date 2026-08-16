package com.elementary.tasks.groups

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.ads.AdBanner
import com.elementary.tasks.ads.NormalAdBanner
import com.elementary.tasks.groups.details.GroupDetailsScreen
import com.elementary.tasks.groups.details.GroupDetailsState
import com.elementary.tasks.groups.details.GroupDetailsViewModel
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.feature.reminder.preview.ReminderPreviewNavKey
import com.github.naz013.group.GroupsNavKey
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Registers the group details entry. This stays in `app` rather than `feature-group` because it
 * reuses [com.github.naz013.feature.reminder.lists.data.UiReminderList] and navigates into the
 * reminder-build/reminder-preview features, none of which are extracted yet.
 */
fun EntryProviderScope<NavKey>.groupDetailsEntries(backStack: MutableList<NavKey>) {
  entry<GroupsNavKey.Details> { key -> GroupsDetailsEntry(key, backStack) }
}

@Composable
private fun GroupsDetailsEntry(
  key: GroupsNavKey.Details,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<GroupDetailsViewModel> { parametersOf(key.id) }
  val dialogDispatcher = rememberDialogDispatcher()

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(viewModel, lifecycleOwner) {
    val observer =
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          viewModel.refreshState()
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is GroupDetailsViewModel.NavigationEvent.OpenEdit -> backStack.add(GroupsNavKey.Edit(event.id))
      is GroupDetailsViewModel.NavigationEvent.OpenReminderPreview ->
        backStack.add(ReminderPreviewNavKey.Preview(event.id))
      is GroupDetailsViewModel.NavigationEvent.ConfirmDelete -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.delete_group_permanently,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.onDeleteConfirmed() }
        )
      }
      is GroupDetailsViewModel.NavigationEvent.OpenAddReminder -> {
        backStack.add(BuildReminderNavKey.Main(groupUuId = event.groupUuId))
      }
      GroupDetailsViewModel.NavigationEvent.Deleted -> if (backStack.size > 1) backStack.removeLastOrNull()
    }
  }

  val state by viewModel.state.collectAsState(GroupDetailsState())
  GroupDetailsScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onEditClick = viewModel::onEditClick,
    onDeleteClick = viewModel::onDeleteClick,
    onReminderClick = viewModel::onReminderClick,
    onAddClick = viewModel::onAddReminderClicked,
    adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), adBanner = AdBanner.Group) },
  )
}
