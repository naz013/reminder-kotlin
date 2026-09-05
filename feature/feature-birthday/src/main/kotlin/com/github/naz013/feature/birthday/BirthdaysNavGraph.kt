package com.github.naz013.feature.birthday

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.common.Permissions
import com.github.naz013.feature.birthday.create.EditBirthdayScreen
import com.github.naz013.feature.birthday.create.EditBirthdayState
import com.github.naz013.feature.birthday.create.EditBirthdayViewModel
import com.github.naz013.feature.birthday.list.BirthdaysScreen
import com.github.naz013.feature.birthday.list.BirthdaysScreenState
import com.github.naz013.feature.birthday.list.BirthdaysViewModel
import com.github.naz013.feature.birthday.preview.PreviewBirthdayScreen
import com.github.naz013.feature.birthday.preview.PreviewBirthdayState
import com.github.naz013.feature.birthday.preview.PreviewBirthdayViewModel
import com.github.naz013.tags.TagsNavKey
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.navigation.DetailPanePlaceholder
import com.github.naz013.ui.common.compose.foundation.navigation.sidePanelSupporting
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberUndoSnackbarDispatcher
import com.github.naz013.ui.common.datetime.rememberDateTimePicker
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.birthdaysEntries(
  backStack: MutableList<NavKey>,
  isRenderedAsDetailPane: (NavKey) -> Boolean,
  isRenderedAsSidePanel: (NavKey) -> Boolean,
  adsContent: @Composable () -> Unit,
  onCallClick: (String) -> Unit,
  onSmsClick: (String) -> Unit,
) {
  entry<BirthdaysNavKey.List>(
    metadata = ListDetailSceneStrategy.listPane(
      detailPlaceholder = {
        DetailPanePlaceholder(
          text = stringResource(R.string.select_birthday_to_see_details),
          icon = AppIcons.Fluent.FoodCake,
        )
      },
    ),
  ) { ListEntry(backStack) }
  entry<BirthdaysNavKey.Preview>(
    metadata = ListDetailSceneStrategy.detailPane() + sidePanelSupporting(),
  ) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) || isRenderedAsSidePanel(key) }
    PreviewEntry(key, backStack, renderAsDetailPane, adsContent, onCallClick, onSmsClick)
  }
  entry<BirthdaysNavKey.Edit>(
    metadata = ListDetailSceneStrategy.detailPane() + sidePanelSupporting(),
  ) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) || isRenderedAsSidePanel(key) }
    EditEntry(key, backStack, renderAsDetailPane, adsContent)
  }
}

@Composable
private fun ListEntry(
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<BirthdaysViewModel>()
  val dialogDispatcher = rememberDialogDispatcher()
  val snackbarHostState = remember { SnackbarHostState() }
  val undoSnackbarDispatcher = rememberUndoSnackbarDispatcher(snackbarHostState)
  val undoActionLabel = stringResource(R.string.undo)

  val selectedItemId =
    backStack.lastOrNull()?.let { key ->
      when (key) {
        is BirthdaysNavKey.Preview -> key.id
        is BirthdaysNavKey.Edit -> key.id
        else -> null
      }
    }
  LaunchedEffect(selectedItemId) { viewModel.onSelectedItemIdChanged(selectedItemId) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is BirthdaysViewModel.NavigationEvent.OpenPreview -> {
        backStack.navigateToDetailPane(BirthdaysNavKey.Preview(event.id))
      }

      is BirthdaysViewModel.NavigationEvent.OpenEdit -> {
        backStack.navigateToEditDetailPane(BirthdaysNavKey.Edit(event.id)) {
          it is BirthdaysNavKey.Preview && it.id == event.id
        }
      }

      is BirthdaysViewModel.NavigationEvent.OpenNewBirthday -> {
        backStack.navigateToDetailPane(BirthdaysNavKey.Edit())
      }

      is BirthdaysViewModel.NavigationEvent.ConfirmDeleteSelected -> {
        dialogDispatcher.showDialog(
          text = event.title,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.deleteSelectedBirthdays(event.ids) },
        )
      }

      is BirthdaysViewModel.NavigationEvent.ShowUndoDelete -> {
        undoSnackbarDispatcher.showUndoSnackbar(
          message = event.message,
          actionLabel = undoActionLabel,
          onUndo = { viewModel.undoDelete(event.batchKey) },
          onTimeout = { viewModel.commitDelete(event.batchKey) },
        )
      }
    }
  }

  val state by viewModel.state.collectAsState(BirthdaysScreenState())
  BirthdaysScreen(
    state = state,
    snackbarHostState = snackbarHostState,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onSmartListSelected = viewModel::onSmartListSelected,
    onTagFilterSelected = viewModel::onTagFilterSelected,
    onAddClick = viewModel::onAddClick,
    onItemClick = viewModel::onItemClick,
    onItemLongClick = viewModel::onItemLongClick,
    onMenuAction = viewModel::onMenuAction,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onDeleteDismiss = viewModel::onDeleteDismiss,
    onSelectionCancel = viewModel::onSelectionCancel,
    onDeleteSelectedClick = viewModel::onDeleteSelectedClick,
  )
}

@Composable
private fun PreviewEntry(
  key: BirthdaysNavKey.Preview,
  backStack: MutableList<NavKey>,
  renderAsDetailPane: Boolean,
  adsContent: @Composable () -> Unit,
  onCallClick: (String) -> Unit,
  onSmsClick: (String) -> Unit,
) {
  val viewModel = koinViewModel<PreviewBirthdayViewModel> { parametersOf(key.id) }

  val permissionRequester = rememberPermissionRequesterRationale()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is PreviewBirthdayViewModel.ViewModelEvent.MoveBack -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
      }

      is PreviewBirthdayViewModel.ViewModelEvent.MakeCall -> {
        permissionRequester.request(Permissions.CALL_PHONE, onGranted = { onCallClick(event.number) })
      }

      is PreviewBirthdayViewModel.ViewModelEvent.SendSms -> {
        onSmsClick(event.number)
      }
    }
  }

  val state by viewModel.state.collectAsState(PreviewBirthdayState())
  PreviewBirthdayScreen(
    state = state,
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    // Pushed on top rather than replacing Preview: ListDetailSceneStrategy's detail pane always
    // renders the topmost detailPane()-tagged entry, so Edit shows in the pane exactly as Preview
    // did, with Preview left underneath. That way the edit screen's plain single-entry back arrow
    // naturally reveals Preview again afterward, in both single-pane and two-pane.
    onEditClick = { backStack.add(BirthdaysNavKey.Edit(key.id)) },
    onDeleteClick = viewModel::onDeleteClick,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onDeleteDismiss = viewModel::onDeleteDismiss,
    onCallClick = viewModel::onCallClicked,
    onSmsClick = viewModel::onSmsClicked,
    adsContent = adsContent,
  )
}

@Composable
private fun EditEntry(
  key: BirthdaysNavKey.Edit,
  backStack: MutableList<NavKey>,
  renderAsDetailPane: Boolean,
  adsContent: @Composable () -> Unit,
) {
  val viewModel = koinViewModel<EditBirthdayViewModel> { parametersOf(key) }
  val context = LocalContext.current
  val dateTimePicker = rememberDateTimePicker()
  val permissionRequester = rememberPermissionRequesterRationale()
  val pickContact = rememberContactPicker(viewModel::onContactPicked)

  DisposableEffect(viewModel) {
    onDispose { context.hideKeyboard() }
  }

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is EditBirthdayViewModel.ViewModelEvent.MoveBack -> if (backStack.size > 1) backStack.removeLastOrNull()

      is EditBirthdayViewModel.ViewModelEvent.OpenDatePicker -> {
        dateTimePicker.showDatePicker(
          date = event.date,
          title = event.title,
          onDateSelected = { viewModel.onDateChanged(it) },
        )
      }

      is EditBirthdayViewModel.ViewModelEvent.OpenManageTags -> backStack.add(TagsNavKey.Manage)
    }
  }

  val state by viewModel.state.collectAsState(EditBirthdayState())

  EditBirthdayScreen(
    state = state,
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onSaveClick = {
      if (state.number.isNotEmpty()) {
        permissionRequester.request(Permissions.READ_CONTACTS, onGranted = { viewModel.onSaveClick() })
      } else {
        viewModel.onSaveClick()
      }
    },
    onDeleteMenuClick = viewModel::onDeleteMenuClick,
    onNameChange = viewModel::onNameChanged,
    onYearCheckChanged = viewModel::onYearCheckChanged,
    onDateFieldClick = viewModel::onDateClicked,
    onNumberChange = viewModel::onNumberChanged,
    onPickContactClick = {
      permissionRequester.request(Permissions.READ_CONTACTS, onGranted = { pickContact() })
    },
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onCopyKeepClick = viewModel::onCopyKeepClick,
    onCopyReplaceClick = viewModel::onCopyReplaceClick,
    onDialogDismiss = viewModel::onDialogDismiss,
    onTagToggle = viewModel::onTagToggle,
    onManageTagsClick = viewModel::onManageTagsClick,
    adsContent = adsContent,
  )
}

/** Best-effort IME dismiss for a promoted Nav3 screen leaving composition - no Fragment needed. */
private fun android.content.Context.hideKeyboard() {
  val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
  (this as? android.app.Activity)?.window?.currentFocus?.windowToken?.let { imm?.hideSoftInputFromWindow(it, 0) }
}

/**
 * Navigation for the birthdays two-pane list's detail pane: if the current top entry is itself a
 * birthday preview or edit form, replace it instead of stacking another one on top. Mirrors
 * `AppNavGraph.kt`'s identically-purposed private helper - kept local here since List/Preview/Edit
 * are all registered by this same graph, unlike Agenda/Home which reach Preview/Edit through
 * cross-feature lambdas.
 */
private fun MutableList<NavKey>.navigateToDetailPane(key: NavKey) {
  val top = lastOrNull()
  if (top is BirthdaysNavKey.Preview || top is BirthdaysNavKey.Edit) {
    removeLastOrNull()
  }
  add(key)
}

/**
 * Navigation into an Edit screen from the birthdays detail pane: if the detail pane is currently
 * showing a Preview of that very same item ([isSameItemPreview] matches the top entry), push Edit
 * on top of it instead of replacing it - see the matching comment in `AppNavGraph.kt`.
 */
private fun MutableList<NavKey>.navigateToEditDetailPane(key: NavKey, isSameItemPreview: (NavKey) -> Boolean) {
  val top = lastOrNull()
  if (top != null && isSameItemPreview(top)) {
    add(key)
  } else {
    navigateToDetailPane(key)
  }
}
