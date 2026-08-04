package com.elementary.tasks.birthdays

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.ads.AdBanner
import com.elementary.tasks.ads.NormalAdBanner
import com.elementary.tasks.birthdays.create.EditBirthdayScreen
import com.elementary.tasks.birthdays.create.EditBirthdayState
import com.elementary.tasks.birthdays.create.EditBirthdayViewModel
import com.elementary.tasks.birthdays.preview.PreviewBirthdayScreen
import com.elementary.tasks.birthdays.preview.PreviewBirthdayState
import com.elementary.tasks.birthdays.preview.PreviewBirthdayViewModel
import com.elementary.tasks.core.os.compose.rememberPermissionRequesterRationale
import com.elementary.tasks.core.os.datapicker.compose.rememberContactPicker
import com.elementary.tasks.core.utils.ui.compose.rememberDateTimePicker
import com.elementary.tasks.navigation.nav3.hideKeyboard
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.telephony.rememberPhoneCaller
import com.elementary.tasks.telephony.rememberSmsSender
import com.github.naz013.common.Permissions
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.birthdaysEntries(backStack: MutableList<NavKey>) {
  entry<BirthdaysNavKey.Preview> { key -> PreviewEntry(key, backStack) }
  entry<BirthdaysNavKey.Edit> { key -> EditEntry(key, backStack) }
}

@Composable
private fun PreviewEntry(
  key: BirthdaysNavKey.Preview,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<PreviewBirthdayViewModel> { parametersOf(key.id) }

  val permissionRequester = rememberPermissionRequesterRationale()
  val phoneCaller = rememberPhoneCaller()
  val smsSender = rememberSmsSender()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is PreviewBirthdayViewModel.ViewModelEvent.MoveBack -> {
        backStack.removeLastOrNull()
      }

      is PreviewBirthdayViewModel.ViewModelEvent.MakeCall -> {
        permissionRequester.request(Permissions.CALL_PHONE, onGranted = { phoneCaller.call(event.number) })
      }

      is PreviewBirthdayViewModel.ViewModelEvent.SendSms -> {
        smsSender.send(event.number, null)
      }
    }
  }

  val state by viewModel.state.collectAsState(PreviewBirthdayState())
  PreviewBirthdayScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onEditClick = { backStack.add(BirthdaysNavKey.Edit(key.id)) },
    onDeleteClick = viewModel::onDeleteClick,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onDeleteDismiss = viewModel::onDeleteDismiss,
    onCallClick = viewModel::onCallClicked,
    onSmsClick = viewModel::onSmsClicked,
    adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.Birthday) },
  )
}

@Composable
private fun EditEntry(
  key: BirthdaysNavKey.Edit,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<EditBirthdayViewModel> { parametersOf(key) }
  val context = LocalContext.current
  val dateTimePicker = rememberDateTimePicker()
  val permissionRequester = rememberPermissionRequesterRationale()
  val pickContact = rememberContactPicker(onContactPicked = viewModel::onContactPicked)

  DisposableEffect(viewModel) {
    onDispose { context.hideKeyboard() }
  }

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is EditBirthdayViewModel.ViewModelEvent.MoveBack -> backStack.removeLastOrNull()

      is EditBirthdayViewModel.ViewModelEvent.OpenDatePicker -> {
        dateTimePicker.showDatePicker(
          date = event.date,
          title = event.title,
          onDateSelected = { viewModel.onDateChanged(it) },
        )
      }
    }
  }

  val state by viewModel.state.collectAsState(EditBirthdayState())

  EditBirthdayScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
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
    onPickContactClick = pickContact,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onCopyKeepClick = viewModel::onCopyKeepClick,
    onCopyReplaceClick = viewModel::onCopyReplaceClick,
    onDialogDismiss = viewModel::onDialogDismiss,
  )
}
