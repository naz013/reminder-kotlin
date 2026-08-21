package com.github.naz013.feature.settings.other

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.settings.SettingsScaffold
import com.github.naz013.feature.settings.other.whatsnew.WhatsNewScreen
import com.github.naz013.feature.settings.other.whatsnew.WhatsNewState
import com.github.naz013.feature.settings.other.whatsnew.WhatsNewViewModel
import com.github.naz013.feature.settings.proversion.rememberGooglePlayMarketLauncher
import com.github.naz013.ui.common.compose.foundation.intent.rememberSendIntentResolver
import com.github.naz013.ui.common.compose.foundation.telephony.rememberUrlLauncher
import com.github.naz013.reviews.rememberReviewsFormLauncher
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.dialog.rememberListDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.otherEntries(
  backStack: MutableList<NavKey>,
  onOpenTroubleshooting: () -> Unit,
  onOpenProVersion: () -> Unit,
) {
  entry<OtherNavKey.Other> { OtherEntry(backStack, onOpenTroubleshooting, onOpenProVersion) }
  entry<OtherNavKey.Permissions> { PermissionsEntry(backStack) }
  entry<OtherNavKey.Oss> { OssEntry(backStack) }
  entry<OtherNavKey.PrivacyPolicy> { PrivacyPolicyEntry(backStack) }
  entry<OtherNavKey.Terms> { TermsEntry(backStack) }
  entry<OtherNavKey.WhatsNew> { WhatsNewEntry(backStack) }
  entry<OtherNavKey.GeminiFunctions> { GeminiFunctionsEntry(backStack) }
}

@Composable
private fun OtherEntry(
  backStack: MutableList<NavKey>,
  onOpenTroubleshooting: () -> Unit,
  onOpenProVersion: () -> Unit,
) {
  val viewModel = koinViewModel<OtherSettingsViewModel>()

  val googlePlayMarketLauncher = rememberGooglePlayMarketLauncher()
  val reviewsFormLauncher = rememberReviewsFormLauncher()
  val toastDispatcher = rememberToastDispatcher()
  val listDialogDispatcher = rememberListDialogDispatcher()
  val permissionRequester = rememberPermissionRequesterRationale()
  val sendIntentResolver = rememberSendIntentResolver()
  val urlLauncher = rememberUrlLauncher()

  val state by viewModel.state.collectAsState(OtherSettingsState())

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is OtherSettingsViewModel.ViewModelEvent.ShowPermissionDialog -> {
        listDialogDispatcher.showDialog(
          titleRes = R.string.allow_permission,
          items = event.permissions.map { it.title },
          onItemClick = { index ->
            val item = event.permissions[index]
            permissionRequester.request(
              permission = item.permission,
              onGranted = { viewModel.onShowPermissionDialogClicked() },
            )
          },
        )
      }

      is OtherSettingsViewModel.ViewModelEvent.ShowToast -> {
        toastDispatcher.showToast(message = event.message)
      }

      is OtherSettingsViewModel.ViewModelEvent.ShowFeedbackDialog -> {
        reviewsFormLauncher.showFeedbackForm(
          title = event.title,
          appSource = event.appSource,
          allowLogsAttachment = event.allowLogsAttachment,
        )
      }

      is OtherSettingsViewModel.ViewModelEvent.ShareApp -> {
        sendIntentResolver.resolve(event.intent, event.title)
      }

      is OtherSettingsViewModel.ViewModelEvent.OpenUrl -> {
        urlLauncher.launch(event.url)
      }
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.other),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    OtherSettingsScreen(
      state = state,
      onPrivacyPolicyClick = { backStack.add(OtherNavKey.PrivacyPolicy) },
      onTermsClick = { backStack.add(OtherNavKey.Terms) },
      onTroubleshootingClick = onOpenTroubleshooting,
      onFeedbackClick = { viewModel.onFeedbackClicked() },
      onRateClick = { googlePlayMarketLauncher.launchSelf() },
      onTellFriendsClick = { viewModel.onShareClicked() },
      onWhatsNewClick = { backStack.add(OtherNavKey.WhatsNew) },
      onGeminiFunctionsClick = {
        if (state.isGeminiFunctionsLocked) {
          viewModel.onGeminiFunctionsLockedClick()
          onOpenProVersion()
        } else {
          backStack.add(OtherNavKey.GeminiFunctions)
        }
      },
      onBuyMeACoffeeClick = { viewModel.onBuyMeACoffeeClicked() },
      onPermissionsClick = { backStack.add(OtherNavKey.Permissions) },
      onAllowPermissionClick = { viewModel.onShowPermissionDialogClicked() },
      onOssClick = { backStack.add(OtherNavKey.Oss) },
      onAboutClick = viewModel::onAboutClick,
      onAboutDialogDismiss = viewModel::onAboutDialogDismiss,
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun PermissionsEntry(backStack: MutableList<NavKey>) {
  SettingsScaffold(
    title = stringResource(R.string.permissions),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    SettingsWebView(
      url = "file:///android_asset/files/permissions.html",
      modifier = Modifier.padding(padding)
    )
  }
}

@Composable
private fun OssEntry(backStack: MutableList<NavKey>) {
  SettingsScaffold(
    title = stringResource(R.string.open_source_licenses),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    SettingsWebView(
      url = "file:///android_asset/files/oss.html",
      modifier = Modifier.padding(padding)
    )
  }
}

@Composable
private fun PrivacyPolicyEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<PrivacyPolicyViewModel>()
  SettingsScaffold(
    title = stringResource(R.string.privacy_policy),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    SettingsWebView(url = viewModel.url, modifier = Modifier.padding(padding))
  }
}

@Composable
private fun TermsEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<TermsViewModel>()
  SettingsScaffold(
    title = stringResource(R.string.terms_and_conditions),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    SettingsWebView(url = viewModel.url, modifier = Modifier.padding(padding))
  }
}

@Composable
private fun GeminiFunctionsEntry(backStack: MutableList<NavKey>) {
  SettingsScaffold(
    title = stringResource(R.string.gemini_functions),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    SettingsWebView(
      url = "file:///android_asset/files/app_functions.html",
      modifier = Modifier.padding(padding)
    )
  }
}

@Composable
private fun WhatsNewEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<WhatsNewViewModel>()
  val state by viewModel.state.collectAsState(WhatsNewState())
  WhatsNewScreen(
    versionAndDate = state.versionName + "\n" + state.lastUpdated,
    whatsNewText = state.whatsNewText,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
