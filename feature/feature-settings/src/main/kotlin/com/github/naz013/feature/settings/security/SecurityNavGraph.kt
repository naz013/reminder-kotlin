package com.github.naz013.feature.settings.security

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.settings.SettingsScaffold
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.login.rememberBiometricProvider
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.securityEntries(backStack: MutableList<NavKey>) {
  entry<SecurityNavKey.Security> { SecurityEntry(backStack) }
  entry<SecurityNavKey.AddPin> { AddPinEntry(backStack) }
  entry<SecurityNavKey.ChangePin> { ChangePinEntry(backStack) }
  entry<SecurityNavKey.DisablePin> { DisablePinEntry(backStack) }
}

@Composable
private fun SecurityEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<SecuritySettingsViewModel>()

  val biometricProvider = rememberBiometricProvider()
  val state by viewModel.state.collectAsState(SecuritySettingsState())

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      SecuritySettingsEvent.OpenAddPin -> backStack.add(SecurityNavKey.AddPin)
      SecuritySettingsEvent.OpenDisablePin -> backStack.add(SecurityNavKey.DisablePin)
      SecuritySettingsEvent.OpenChangePin -> backStack.add(SecurityNavKey.ChangePin)
      SecuritySettingsEvent.TryBiometricLogin -> {
        biometricProvider.authenticate(
          onSuccess = {
            viewModel.onBiometricAuthSuccess()
          },
        )
      }
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.security),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    SecuritySettingsScreen(
      state = state,
      onPinRowClick = viewModel::onPinRowClick,
      onChangePinClick = viewModel::onChangePinClick,
      onFingerprintClick = viewModel::onBiometricAuthClicked,
      onShuffleToggle = viewModel::onShuffleToggle,
      onTelephonyToggle = viewModel::onTelephonyToggle,
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun AddPinEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<AddPinViewModel>()
  val toastDispatcher = rememberToastDispatcher()
  val state by viewModel.state.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      AddPinEvent.ShowPinMismatch -> {
        toastDispatcher.showToast(messageRes = R.string.pin_not_match)
      }

      AddPinEvent.PinSaved -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
      }
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.add_pin),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    AddPinScreen(
      stage = state.stage,
      pin = state.pin,
      onDigitClick = viewModel::onDigitClick,
      onDeleteClick = viewModel::onDeleteClick,
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun ChangePinEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<ChangePinViewModel>()
  val toastDispatcher = rememberToastDispatcher()
  val state by viewModel.state.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      ChangePinEvent.ShowPinMismatch -> {
        toastDispatcher.showToast(messageRes = R.string.pin_not_match)
      }

      ChangePinEvent.PinSaved -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
      }
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.change_pin),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    ChangePinScreen(
      stage = state.stage,
      pin = state.pin,
      onDigitClick = viewModel::onDigitClick,
      onDeleteClick = viewModel::onDeleteClick,
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun DisablePinEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<DisablePinViewModel>()
  val toastDispatcher = rememberToastDispatcher()
  val state by viewModel.state.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      DisablePinEvent.ShowPinMismatch -> {
        toastDispatcher.showToast(messageRes = R.string.pin_not_match)
      }

      DisablePinEvent.PinCleared -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
      }
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.disable_pin),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    DisablePinScreen(
      pin = state.pin,
      onDigitClick = viewModel::onDigitClick,
      onDeleteClick = viewModel::onDeleteClick,
      modifier = Modifier.padding(padding),
    )
  }
}
