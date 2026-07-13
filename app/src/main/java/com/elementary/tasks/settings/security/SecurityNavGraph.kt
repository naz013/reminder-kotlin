package com.elementary.tasks.settings.security

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.common.system.Module
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.login.BiometricProvider
import com.elementary.tasks.settings.SettingsScaffold
import org.koin.compose.viewmodel.koinViewModel

/**
 * Contributes the Security/PIN Settings sub-tree's screens (Nav3 entries) into the app's single,
 * shared [androidx.navigation3.ui.NavDisplay] (see
 * [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
fun EntryProviderScope<NavKey>.securityEntries(backStack: MutableList<NavKey>) {
  entry<SecurityNavKey.Security> { SecurityEntry(backStack) }
  entry<SecurityNavKey.AddPin> { AddPinEntry(backStack) }
  entry<SecurityNavKey.ChangePin> { ChangePinEntry(backStack) }
  entry<SecurityNavKey.DisablePin> { DisablePinEntry(backStack) }
}

@Composable
private fun SecurityEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<SecuritySettingsViewModel>()
  val context = LocalContext.current
  val activity = LocalActivity.current as FragmentActivity
  val biometricProvider = remember(activity, viewModel) { BiometricProvider(activity) { viewModel.onFingerprintAuthSucceeded() } }
  val hasBiometricHardware = remember { biometricProvider.hasBiometric() }
  val hasTelephony = remember { Module.hasTelephony(context) }
  val state by viewModel.state.collectAsState()

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(viewModel, lifecycleOwner) {
    val observer =
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume(Module.hasTelephony(context))
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      SecuritySettingsEvent.OpenAddPin -> backStack.add(SecurityNavKey.AddPin)
      SecuritySettingsEvent.OpenDisablePin -> backStack.add(SecurityNavKey.DisablePin)
      SecuritySettingsEvent.OpenChangePin -> backStack.add(SecurityNavKey.ChangePin)
      SecuritySettingsEvent.TryFingerprintLogin -> biometricProvider.tryToOpenFingerLogin()
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.security),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    SecuritySettingsScreen(
      state = state,
      hasBiometricHardware = hasBiometricHardware,
      hasTelephony = hasTelephony,
      onPinRowClick = viewModel::onPinRowClick,
      onChangePinClick = viewModel::onChangePinClick,
      onFingerprintClick = viewModel::onFingerprintClick,
      onShuffleToggle = viewModel::onShuffleToggle,
      onTelephonyToggle = viewModel::onTelephonyToggle,
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun AddPinEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<AddPinViewModel>()
  val activity = LocalActivity.current as FragmentActivity
  val state by viewModel.state.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      AddPinEvent.ShowPinMismatch -> activity.toast(R.string.pin_not_match)
      AddPinEvent.PinSaved -> backStack.removeLastOrNull()
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.add_pin),
    onBackClick = { backStack.removeLastOrNull() },
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
  val activity = LocalActivity.current as FragmentActivity
  val state by viewModel.state.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      ChangePinEvent.ShowPinMismatch -> activity.toast(R.string.pin_not_match)
      ChangePinEvent.PinSaved -> backStack.removeLastOrNull()
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.change_pin),
    onBackClick = { backStack.removeLastOrNull() },
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
  val activity = LocalActivity.current as FragmentActivity
  val state by viewModel.state.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      DisablePinEvent.ShowPinMismatch -> activity.toast(R.string.pin_not_match)
      DisablePinEvent.PinCleared -> backStack.removeLastOrNull()
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.disable_pin),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    DisablePinScreen(
      pin = state.pin,
      onDigitClick = viewModel::onDigitClick,
      onDeleteClick = viewModel::onDeleteClick,
      modifier = Modifier.padding(padding),
    )
  }
}
