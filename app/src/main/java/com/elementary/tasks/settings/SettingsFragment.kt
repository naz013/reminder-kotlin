package com.elementary.tasks.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.github.naz013.ui.common.login.LoginLauncher
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : BaseComposeToolbarFragment() {

  private val viewModel by viewModel<SettingsHubViewModel>()
  private val loginLauncher = LoginLauncher(this) { if (it) openSecurity() }

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(viewModel) { lifecycle.addObserver(viewModel) }
    val isPlayServicesWarningVisible = remember { !SuperUtil.isGooglePlayServicesAvailable(requireContext()) }
    val isBuyProBadgeVisible = remember {
      !BuildParams.isPro && !SuperUtil.isAppInstalled(requireContext(), "com.cray.software.justreminderpro")
    }

    SettingsHubScreen(
      state = state,
      isBuyProBadgeVisible = isBuyProBadgeVisible,
      isPlayServicesWarningVisible = isPlayServicesWarningVisible,
      onBuyProClick = { safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToProVersionFragment()) },
      onUpdateClick = { withActivity { SuperUtil.launchMarket(it) } },
      onGeneralClick = {
        safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToGeneralSettingsFragment())
      },
      onCloudBackupClick = {
        safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToExportSettingsFragment())
      },
      onCalendarClick = {
        safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToCalendarSettingsFragment(null))
      },
      onRemindersClick = {
        safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToRemindersSettingsFragment(null))
      },
      onBirthdaysClick = {
        safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToBirthdaySettingsFragment())
      },
      onSecurityClick = ::askPin,
      onNotesClick = {
        safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToNoteSettingsFragment(null))
      },
      onOtherClick = {
        safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToOtherSettingsFragment())
      },
      onDeveloperClick = {
        safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToDeveloperFragment())
      },
    )
  }

  private fun askPin() {
    withActivity {
      if (prefs.hasPinCode) {
        loginLauncher.askLogin()
      } else {
        openSecurity()
      }
    }
  }

  private fun openSecurity() {
    safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToSecuritySettingsFragment())
  }

  override fun getTitle(): String = getString(R.string.action_settings)
}
