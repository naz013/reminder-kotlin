package com.elementary.tasks.groups

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation.fragment.findNavController
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.elementary.tasks.R
import com.elementary.tasks.groups.create.EditGroupScreen
import com.elementary.tasks.groups.create.EditGroupState
import com.elementary.tasks.groups.create.EditGroupViewModel
import com.elementary.tasks.groups.list.GroupsScreen
import com.elementary.tasks.groups.list.GroupsScreenState
import com.elementary.tasks.groups.list.GroupsViewModel
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.ui.common.fragment.hideKeyboard
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Builds the Groups island's [NavDisplay] — the "screens" (Nav3 entries) themselves and the
 * routing between them. [GroupsFragment] only owns the backstack and the Android-framework glue
 * (dialogs) that these entries react to.
 */
@Composable
internal fun GroupsFragment.GroupsNavGraph(backStack: MutableList<NavKey>) {
  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryDecorators =
      listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
    transitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_ENTER_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_EXIT_SCALE)
      )
    },
    popTransitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_EXIT_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_ENTER_SCALE)
      )
    },
    predictivePopTransitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_EXIT_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_ENTER_SCALE)
      )
    },
    entryProvider =
      entryProvider {
        entry<GroupsNavKey.List> { GroupsListEntry(backStack) }
        entry<GroupsNavKey.Edit> { key -> GroupsEditEntry(key, backStack) }
      },
  )
}

private fun navScreenSpring() =
  spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
  )

private const val NAV_ANIM_FADE_DURATION_MS = 250
private const val NAV_ANIM_ENTER_SCALE = 0.92f
private const val NAV_ANIM_EXIT_SCALE = 1.08f

@Composable
private fun GroupsFragment.GroupsListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<GroupsViewModel>()
  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      GroupsViewModel.NavigationEvent.AddGroup -> backStack.add(GroupsNavKey.Edit())
      is GroupsViewModel.NavigationEvent.OpenEdit -> backStack.add(GroupsNavKey.Edit(event.id))
      is GroupsViewModel.NavigationEvent.ConfirmDelete -> {
        dialogues.askConfirmation(requireContext(), getString(R.string.delete)) { confirmed ->
          if (confirmed) viewModel.deleteGroup(event.id)
        }
      }
    }
  }

  val state by viewModel.state.collectAsState(GroupsScreenState())
  GroupsScreen(
    state = state,
    onBackClick = { findNavController().popBackStack() },
    onAddClick = viewModel::onAddClick,
    onGroupClick = viewModel::onGroupClick,
    onGroupMenuAction = viewModel::onGroupMenuAction,
  )
}

@Composable
private fun GroupsFragment.GroupsEditEntry(
  key: GroupsNavKey.Edit,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<EditGroupViewModel> { parametersOf(key.id, arguments) }
  DisposableEffect(viewModel) {
    onDispose { hideKeyboard() }
  }
  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is EditGroupViewModel.NavigationEvent.Back -> backStack.removeLastOrNull()
    }
  }

  val state by viewModel.state.collectAsState(EditGroupState())
  EditGroupScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onSaveClick = viewModel::onSaveClick,
    onDeleteMenuClick = viewModel::onDeleteMenuClick,
    onNameChange = viewModel::onNameChanged,
    onColorSelected = viewModel::onColorSelected,
    onDefaultCheckChanged = viewModel::onDefaultCheckChanged,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onCopyKeepClick = viewModel::onCopyKeepClick,
    onCopyReplaceClick = viewModel::onCopyReplaceClick,
    onDialogDismiss = viewModel::onDialogDismiss,
  )
}
