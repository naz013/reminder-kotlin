package com.elementary.tasks.settings.other

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.core.os.compose.PermissionRationaleDialog
import com.elementary.tasks.core.os.compose.rememberPermissionRequester
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.settings.SettingsNavKey
import com.elementary.tasks.settings.SettingsScaffold
import com.elementary.tasks.settings.other.whatsnew.WhatsNewScreen
import com.elementary.tasks.settings.other.whatsnew.WhatsNewState
import com.elementary.tasks.settings.other.whatsnew.WhatsNewViewModel
import com.github.naz013.common.Module
import com.github.naz013.common.Permissions
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.ReviewsApi
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.activity.toast
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Contributes the "Other"/legal/WebView Settings sub-tree's screens (Nav3 entries) into the app's
 * single, shared [androidx.navigation3.ui.NavDisplay] (see
 * [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
fun EntryProviderScope<NavKey>.otherEntries(backStack: MutableList<NavKey>) {
  entry<OtherNavKey.Other> { OtherEntry(backStack) }
  entry<OtherNavKey.Permissions> { PermissionsEntry(backStack) }
  entry<OtherNavKey.Oss> { OssEntry(backStack) }
  entry<OtherNavKey.PrivacyPolicy> { PrivacyPolicyEntry(backStack) }
  entry<OtherNavKey.Terms> { TermsEntry(backStack) }
  entry<OtherNavKey.WhatsNew> { WhatsNewEntry(backStack) }
}

@Composable
private fun OtherEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<OtherSettingsViewModel>()
  val reviewsApi = koinInject<ReviewsApi>()
  val featureManager = koinInject<FeatureManager>()
  val dialogues = koinInject<Dialogues>()
  val activity = LocalActivity.current as FragmentActivity
  val permissionRequester = rememberPermissionRequester()
  val state by viewModel.state.collectAsState()
  var permissionItems by remember { mutableStateOf<List<PermissionItem>>(emptyList()) }

  fun loadPermissionItems(): Boolean {
    val items = buildList {
      fun addIfMissing(titleRes: Int, permission: String) {
        if (!Permissions.checkPermission(activity, permission)) {
          add(PermissionItem(activity.getString(titleRes), permission))
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
      if (Module.is15) addIfMissing(R.string.foreground_service_location, Permissions.FOREGROUND_SERVICE_LOCATION)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        addIfMissing(R.string.post_notification, Permissions.POST_NOTIFICATION)
      }
    }
    return if (items.isEmpty()) {
      activity.toast(R.string.all_permissions_are_enabled)
      false
    } else {
      permissionItems = items
      true
    }
  }

  fun showPermissionDialog() {
    if (!loadPermissionItems()) return
    val builder = dialogues.getMaterialDialog(activity)
    builder.setTitle(R.string.allow_permission)
    val names = permissionItems.map { it.title }
    builder.setItems(names.toTypedArray()) { dialogInterface, i ->
      dialogInterface.dismiss()
      val item = permissionItems[i]
      permissionRequester.request(item.permission, onGranted = { showPermissionDialog() })
    }
    builder.setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
    builder.create().show()
  }

  PermissionRationaleDialog(permissionRequester)
  SettingsScaffold(
    title = stringResource(R.string.other),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    OtherSettingsScreen(
      state = state,
      onPrivacyPolicyClick = { backStack.add(OtherNavKey.PrivacyPolicy) },
      onTermsClick = { backStack.add(OtherNavKey.Terms) },
      onTroubleshootingClick = { backStack.add(SettingsNavKey.Troubleshooting) },
      onFeedbackClick = {
        reviewsApi.showFeedbackForm(
          activity,
          activity.getString(R.string.share_your_experience),
          appSource = if (BuildParams.isPro) AppSource.PRO else AppSource.FREE,
          allowLogsAttachment = featureManager.isFeatureEnabled(FeatureManager.Feature.LOGS_IN_REVIEWS),
        )
      },
      onRateClick = { SuperUtil.launchMarket(activity) },
      onTellFriendsClick = {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + activity.packageName)
        activity.startActivity(Intent.createChooser(shareIntent, "Share..."))
      },
      onWhatsNewClick = { backStack.add(OtherNavKey.WhatsNew) },
      onPermissionsClick = { backStack.add(OtherNavKey.Permissions) },
      onAllowPermissionClick = { showPermissionDialog() },
      onOssClick = { backStack.add(OtherNavKey.Oss) },
      onAboutClick = viewModel::onAboutClick,
      onAboutDialogDismiss = viewModel::onAboutDialogDismiss,
      modifier = Modifier.padding(padding),
    )
  }
}

private class PermissionItem(val title: String, val permission: String)

@Composable
private fun PermissionsEntry(backStack: MutableList<NavKey>) {
  SettingsScaffold(
    title = stringResource(R.string.permissions),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    SettingsWebView(url = "file:///android_asset/files/permissions.html", modifier = Modifier.padding(padding))
  }
}

@Composable
private fun OssEntry(backStack: MutableList<NavKey>) {
  SettingsScaffold(
    title = stringResource(R.string.open_source_licenses),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    SettingsWebView(url = "file:///android_asset/files/oss.html", modifier = Modifier.padding(padding))
  }
}

@Composable
private fun PrivacyPolicyEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<PrivacyPolicyViewModel>()
  SettingsScaffold(
    title = stringResource(R.string.privacy_policy),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    SettingsWebView(url = viewModel.url, modifier = Modifier.padding(padding))
  }
}

@Composable
private fun TermsEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<TermsViewModel>()
  SettingsScaffold(
    title = stringResource(R.string.terms_and_conditions),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    SettingsWebView(url = viewModel.url, modifier = Modifier.padding(padding))
  }
}

@Composable
private fun WhatsNewEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<WhatsNewViewModel>()
  val state by viewModel.state.collectAsState(WhatsNewState())
  WhatsNewScreen(
    versionAndDate = state.versionName + "\n" + state.lastUpdated,
    whatsNewText = state.whatsNewText,
    onBackClick = { backStack.removeLastOrNull() },
  )
}

/** Compose counterpart of the legacy `BaseComposeWebViewFragment`-hosted WebView settings screens. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SettingsWebView(
  url: String,
  modifier: Modifier = Modifier,
) {
  AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { context ->
      WebView(context).apply {
        settings.javaScriptEnabled = true
        webViewClient =
          object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
              view: WebView,
              errorCode: Int,
              description: String,
              failingUrl: String,
            ) = Unit

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(
              view: WebView,
              loadUrl: String,
            ): Boolean {
              view.loadUrl(loadUrl)
              return true
            }
          }
        webChromeClient = WebChromeClient()
        loadUrl(url)
      }
    },
  )
}
