package com.elementary.tasks.settings.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.elementary.tasks.R
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.common.Module
import com.github.naz013.ui.common.login.BiometricProvider
import org.koin.androidx.viewmodel.ext.android.viewModel

class SecuritySettingsFragment : BaseComposeToolbarFragment() {

  private val viewModel by viewModel<SecuritySettingsViewModel>()
  private val biometricProvider = BiometricProvider(this) { viewModel.onFingerprintAuthSucceeded() }

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    viewModel.navigationEvent.ObserveEvent { handleEvent(it) }
    val hasBiometricHardware = remember { biometricProvider.hasBiometric() }
    val hasTelephony = remember { Module.hasTelephony(requireContext()) }

    SecuritySettingsScreen(
      state = state,
      hasBiometricHardware = hasBiometricHardware,
      hasTelephony = hasTelephony,
      onPinRowClick = viewModel::onPinRowClick,
      onChangePinClick = viewModel::onChangePinClick,
      onFingerprintClick = viewModel::onFingerprintClick,
      onShuffleToggle = viewModel::onShuffleToggle,
      onTelephonyToggle = viewModel::onTelephonyToggle,
    )
  }

  private fun handleEvent(event: SecuritySettingsEvent) {
    when (event) {
      SecuritySettingsEvent.OpenAddPin -> {
        safeNavigation { SecuritySettingsFragmentDirections.actionSecuritySettingsFragmentToAddPinFragment() }
      }

      SecuritySettingsEvent.OpenDisablePin -> {
        safeNavigation { SecuritySettingsFragmentDirections.actionSecuritySettingsFragmentToDisablePinFragment() }
      }

      SecuritySettingsEvent.OpenChangePin -> {
        safeNavigation { SecuritySettingsFragmentDirections.actionSecuritySettingsFragmentToChangePinFragment() }
      }

      SecuritySettingsEvent.TryFingerprintLogin -> biometricProvider.tryToOpenFingerLogin()
    }
  }

  override fun onBackStackResumed() {
    super.onBackStackResumed()
    viewModel.onResume(Module.hasTelephony(requireContext()))
  }

  override fun getTitle(): String = getString(R.string.security)
}
