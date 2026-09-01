package com.github.naz013.feature.settings.other

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureGateTappedEvent
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.Permissions
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.digestapi.DigestCapabilityChecker
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.logging.Logger
import com.github.naz013.platform.SystemInfo
import com.github.naz013.reviews.AppSource
import com.github.naz013.ui.common.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class OtherSettingsViewModel(
  private val packageManagerWrapper: PackageManagerWrapper,
  private val textProvider: TextProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val contextProvider: ContextProvider,
  private val systemInfo: SystemInfo,
  private val featureFlags: FeatureFlags,
  private val buildInfo: BuildInfo,
  private val digestCapabilityChecker: DigestCapabilityChecker,
) : ViewModel() {

  private val _state = MutableStateFlow(OtherSettingsState())
  val state = _state.stateInWhileSubscribed(OtherSettingsState())
    .onStart { loadState() }

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.OTHER_SETTINGS))
  }

  fun onShareClicked() {
    Logger.i(TAG, "Share clicked")
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(
      Intent.EXTRA_TEXT,
      "https://play.google.com/store/apps/details?id=" + systemInfo.currentPackageName
    )
    event.emit(
      ViewModelEvent.ShareApp(shareIntent, textProvider.getString(R.string.share_intent_title))
    )
  }

  fun onFeedbackClicked() {
    Logger.i(TAG, "Feedback clicked")
    event.emit(
      ViewModelEvent.ShowFeedbackDialog(
        title = textProvider.getString(R.string.share_your_experience),
        appSource = if (buildInfo.isPro) AppSource.PRO else AppSource.FREE,
        allowLogsAttachment = featureFlags.isEnabled(FeatureFlag.LOGS_IN_REVIEWS),
      )
    )
  }

  fun onGeminiFunctionsLockedClick() {
    analyticsEventSender.send(FeatureGateTappedEvent(Feature.GEMINI_FUNCTIONS))
  }

  fun onDigestLockedClick() {
    analyticsEventSender.send(FeatureGateTappedEvent(Feature.AI_DIGEST))
  }

  fun onBuyMeACoffeeClicked() {
    analyticsEventSender.send(FeatureUsedEvent(Feature.BUY_ME_A_COFFEE))
    event.emit(ViewModelEvent.OpenUrl(SUPPORT_URL))
  }

  fun onShowPermissionDialogClicked() {
    _state.update {
      it.copy(
        permissionItems = loadPermissionItems(),
      )
    }
    if (_state.value.permissionItems.isEmpty()) {
      Logger.v(TAG, "Will not show permission dialog, no permissions are missing")
      event.emit(ViewModelEvent.ShowToast(textProvider.getString(R.string.all_permissions_are_enabled)))
      return
    }
    Logger.i(TAG, "Will show permission dialog")
    event.emit(ViewModelEvent.ShowPermissionDialog(_state.value.permissionItems))
  }

  fun onAboutClick() {
    val appName =
      if (buildInfo.isPro) {
        textProvider.getString(R.string.app_name_pro)
      } else {
        textProvider.getString(R.string.app_name)
      }
    _state.update {
      it.copy(
        aboutDialog =
          AboutDialogState(
            appName = appName.uppercase(),
            version = packageManagerWrapper.getVersionName(),
            translators = textProvider.getStringArray(R.array.app_translators).joinToString("\n"),
          ),
      )
    }
  }

  fun onAboutDialogDismiss() {
    _state.update { it.copy(aboutDialog = null) }
  }

  private fun loadState() {
    _state.update {
      it.copy(
        permissionItems = loadPermissionItems(),
        isGeminiFunctionsVisible = systemInfo.is16,
        isGeminiFunctionsLocked = !buildInfo.isPro,
        isBuyMeACoffeeVisible = featureFlags.isEnabled(FeatureFlag.BUY_ME_A_COFFEE),
        isDigestVisible = isDigestVisible(digestCapabilityChecker.isDeviceCapableCached()),
        isDigestLocked = !buildInfo.isPro,
      )
    }
    // Capability is a genuinely async check (ML Kit model/hardware probe) - the cached read above
    // renders immediately, this refreshes it and re-emits if the live result disagrees.
    viewModelScope.launch {
      val capable = digestCapabilityChecker.refreshCapability()
      _state.update { it.copy(isDigestVisible = isDigestVisible(capable)) }
    }
  }

  private fun isDigestVisible(deviceCapable: Boolean): Boolean =
    featureFlags.isEnabled(FeatureFlag.AI_DIGEST) && deviceCapable

  private fun loadPermissionItems(): List<PermissionItem> {
    return buildList {
      fun addIfMissing(titleRes: Int, permission: String) {
        if (!Permissions.checkPermission(contextProvider.context, permission)) {
          add(PermissionItem(textProvider.getString(titleRes), permission))
        }
      }
      addIfMissing(R.string.course_location, Permissions.ACCESS_COARSE_LOCATION)
      addIfMissing(R.string.fine_location, Permissions.ACCESS_FINE_LOCATION)
      addIfMissing(R.string.call_phone, Permissions.CALL_PHONE)
      addIfMissing(R.string.get_accounts, Permissions.GET_ACCOUNTS)
      addIfMissing(R.string.read_calendar, Permissions.READ_CALENDAR)
      addIfMissing(R.string.write_calendar, Permissions.WRITE_CALENDAR)
      addIfMissing(R.string.read_contacts, Permissions.READ_CONTACTS)
      addIfMissing(R.string.read_external_storage, Permissions.READ_EXTERNAL)
      addIfMissing(R.string.write_external_storage, Permissions.WRITE_EXTERNAL)
      addIfMissing(R.string.record_audio, Permissions.RECORD_AUDIO)
      addIfMissing(R.string.foreground_service, Permissions.FOREGROUND_SERVICE)
      addIfMissing(R.string.background_location, Permissions.BACKGROUND_LOCATION)
      if (systemInfo.is15) {
        addIfMissing(R.string.foreground_service_location, Permissions.FOREGROUND_SERVICE_LOCATION)
      }
      if (systemInfo.is13) {
        addIfMissing(R.string.post_notification, Permissions.POST_NOTIFICATION)
      }
    }
  }

  sealed interface ViewModelEvent {
    data class ShowPermissionDialog(val permissions: List<PermissionItem>) : ViewModelEvent
    data class ShowToast(val message: String) : ViewModelEvent
    data class ShowFeedbackDialog(
      val title: String?,
      val appSource: AppSource,
      val allowLogsAttachment: Boolean
    ) : ViewModelEvent
    data class ShareApp(
      val intent: Intent,
      val title: String
    ) : ViewModelEvent
    data class OpenUrl(val url: String) : ViewModelEvent
  }

  companion object {
    private const val TAG = "OtherSettingsViewModel"

    private const val SUPPORT_URL = "https://buymeacoffee.com/nsystudio"
  }
}
