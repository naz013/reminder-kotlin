package com.github.naz013.feature.birthday

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.birthday.create.EditBirthdayScreen
import com.github.naz013.feature.birthday.create.EditBirthdayState
import com.github.naz013.feature.birthday.create.EditBirthdayViewModel
import com.github.naz013.feature.birthday.preview.PreviewBirthdayScreen
import com.github.naz013.feature.birthday.preview.PreviewBirthdayState
import com.github.naz013.feature.birthday.preview.PreviewBirthdayViewModel
import com.github.naz013.tags.TagsNavKey
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.github.naz013.common.Permissions
import com.github.naz013.ui.common.datetime.rememberDateTimePicker
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.birthdaysEntries(
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit,
  onCallClick: (String) -> Unit,
  onSmsClick: (String) -> Unit,
  rememberContactPicker: @Composable ((ContactData) -> Unit) -> () -> Unit,
) {
  entry<BirthdaysNavKey.Preview> { key -> PreviewEntry(key, backStack, adsContent, onCallClick, onSmsClick) }
  entry<BirthdaysNavKey.Edit> { key -> EditEntry(key, backStack, adsContent, rememberContactPicker) }
}

@Composable
private fun PreviewEntry(
  key: BirthdaysNavKey.Preview,
  backStack: MutableList<NavKey>,
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
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
  adsContent: @Composable () -> Unit,
  rememberContactPicker: @Composable ((ContactData) -> Unit) -> () -> Unit,
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
