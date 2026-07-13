package com.elementary.tasks.birthdays

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.EditBirthdayScreen
import com.elementary.tasks.birthdays.create.EditBirthdayState
import com.elementary.tasks.birthdays.create.EditBirthdayViewModel
import com.elementary.tasks.birthdays.preview.PreviewBirthdayScreen
import com.elementary.tasks.birthdays.preview.PreviewBirthdayState
import com.elementary.tasks.birthdays.preview.PreviewBirthdayViewModel
import com.elementary.tasks.core.os.compose.rememberPermissionRequesterRationale
import com.elementary.tasks.core.os.datapicker.compose.rememberContactPicker
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ui.compose.rememberDateTimePicker
import com.elementary.tasks.navigation.nav3.hideKeyboard
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.common.Permissions
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Contributes the Birthdays island's screens (Nav3 entries) and the routing between them into the
 * app's single, shared [androidx.navigation3.ui.NavDisplay] (see
 * [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
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
  val context = LocalContext.current
  val permissionRequester = rememberPermissionRequesterRationale()
  viewModel.event.ObserveEvent { event ->
    when (event) {
      is PreviewBirthdayViewModel.ViewModelEvent.MoveBack -> {
        backStack.removeLastOrNull()
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
    onCallClick = {
      state.birthday?.number?.let { number ->
        permissionRequester.request(Permissions.CALL_PHONE, onGranted = { TelephonyUtil.makeCall(number, context) })
      }
    },
    onSmsClick = {
      state.birthday?.number?.let { number -> TelephonyUtil.sendSms(number, context) }
    },
    adsContent = { BirthdayAdBanner() },
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
    }
  }

  val state by viewModel.state.collectAsState(EditBirthdayState())
  val selectDateTitle = stringResource(R.string.select_date)

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
    onDateFieldClick = {
      dateTimePicker.showDatePicker(
        date = state.selectedDate,
        title = selectDateTitle,
        onDateSelected = { viewModel.onDateChanged(it) },
      )
    },
    onNumberChange = viewModel::onNumberChanged,
    onPickContactClick = pickContact,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onCopyKeepClick = viewModel::onCopyKeepClick,
    onCopyReplaceClick = viewModel::onCopyReplaceClick,
    onDialogDismiss = viewModel::onDialogDismiss,
  )
}

@Composable
private fun BirthdayAdBanner() {
  if (BuildParams.isPro || !AdsProvider.hasAds()) return
  val context = LocalContext.current
  val adsProvider = remember { AdsProvider() }
  AndroidView(
    modifier = Modifier.fillMaxWidth(),
    factory = { FrameLayout(context) },
    update = { viewGroup -> adsProvider.showBanner(viewGroup, AdsProvider.BIRTHDAY_PREVIEW_BANNER_ID) },
  )
}
