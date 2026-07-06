package com.elementary.tasks.settings

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.github.naz013.common.TextProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SettingsHubViewModel(
  private val remotePrefs: RemotePrefs,
  private val prefs: Prefs,
  private val doNotDisturbManager: DoNotDisturbManager,
  private val textProvider: TextProvider,
) : ViewModel(),
  DefaultLifecycleObserver,
  RemotePrefs.SaleObserver,
  RemotePrefs.UpdateObserver,
  RemotePrefs.MessageObserver {

  val state: StateFlow<SettingsHubState> field = MutableStateFlow(SettingsHubState())

  private val prefsObserver: (String) -> Unit = { checkDoNotDisturb() }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_ENABLED, prefsObserver)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_FROM, prefsObserver)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_TO, prefsObserver)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_IGNORE, prefsObserver)
    remotePrefs.addUpdateObserver(this)
    remotePrefs.addMessageObserver(this)
    if (!BuildParams.isPro) {
      remotePrefs.addSaleObserver(this)
    }
    checkDoNotDisturb()
  }

  override fun onPause(owner: LifecycleOwner) {
    super.onPause(owner)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_ENABLED, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_FROM, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_TO, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_IGNORE, prefsObserver)
    if (!BuildParams.isPro) {
      remotePrefs.removeSaleObserver(this)
    }
    remotePrefs.removeUpdateObserver(this)
    remotePrefs.removeMessageObserver(this)
  }

  override fun onUpdateChanged(hasUpdate: Boolean, version: String) {
    state.update {
      it.copy(updateMessage = if (hasUpdate) textProvider.getString(R.string.new_update_message, version) else null)
    }
  }

  override fun onSaleChanged(showDiscount: Boolean, discount: String, until: String) {
    state.update {
      it.copy(
        saleMessage = if (showDiscount) {
          textProvider.getString(R.string.new_sale_message, discount, until)
        } else {
          null
        },
      )
    }
  }

  override fun onMessageChanged(showMessage: Boolean, message: String) {
    state.update { it.copy(internalMessage = if (showMessage) message else null) }
  }

  private fun checkDoNotDisturb() {
    state.update { it.copy(isDoNotDisturbActive = doNotDisturbManager.applyDoNotDisturb(0)) }
  }
}
