package com.elementary.tasks.settings.other

import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.navigate
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.github.naz013.common.Module
import com.github.naz013.common.Permissions
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.ReviewsApi
import com.github.naz013.ui.common.fragment.toast
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class OtherSettingsFragment : BaseComposeToolbarFragment() {

  private val viewModel by viewModel<OtherSettingsViewModel>()
  private val reviewsApi by inject<ReviewsApi>()
  private val featureManager by inject<FeatureManager>()

  private val permissionItems = ArrayList<PermissionItem>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()

    OtherSettingsScreen(
      state = state,
      onPrivacyPolicyClick = {
        safeNavigation { OtherSettingsFragmentDirections.actionOtherSettingsFragmentToPrivacyPolicyFragment() }
      },
      onTermsClick = {
        safeNavigation { OtherSettingsFragmentDirections.actionOtherSettingsFragmentToTermsFragment() }
      },
      onTroubleshootingClick = {
        navigate { navigate(R.id.troubleshootingFragment, null, NavigationAnimations.inDepthNavOptions()) }
      },
      onFeedbackClick = ::openFeedbackScreen,
      onRateClick = { withActivity { SuperUtil.launchMarket(it) } },
      onTellFriendsClick = ::shareApplication,
      onWhatsNewClick = {
        safeNavigation { OtherSettingsFragmentDirections.actionOtherSettingsFragmentToChangesFragment() }
      },
      onPermissionsClick = {
        safeNavigation { OtherSettingsFragmentDirections.actionOtherSettingsFragmentToPermissionsFragment() }
      },
      onAllowPermissionClick = ::showPermissionDialog,
      onOssClick = {
        safeNavigation { OtherSettingsFragmentDirections.actionOtherSettingsFragmentToOssFragment() }
      },
      onAboutClick = viewModel::onAboutClick,
      onAboutDialogDismiss = viewModel::onAboutDialogDismiss,
    )
  }

  private fun openFeedbackScreen() {
    reviewsApi.showFeedbackForm(
      requireContext(),
      getString(R.string.share_your_experience),
      appSource = if (BuildParams.isPro) AppSource.PRO else AppSource.FREE,
      allowLogsAttachment = featureManager.isFeatureEnabled(FeatureManager.Feature.LOGS_IN_REVIEWS),
    )
  }

  private fun shareApplication() {
    withContext {
      val shareIntent = Intent(Intent.ACTION_SEND)
      shareIntent.type = "text/plain"
      shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + it.packageName)
      context?.startActivity(Intent.createChooser(shareIntent, "Share..."))
    }
  }

  private fun requestPermission(position: Int) {
    permissionFlow.askPermission(permissionItems[position].permission) { showPermissionDialog() }
  }

  private fun loadPermissionItems(): Boolean {
    permissionItems.clear()
    val activity = activity ?: return false

    fun addIfMissing(titleRes: Int, permission: String) {
      if (!Permissions.checkPermission(activity, permission)) {
        permissionItems.add(PermissionItem(getString(titleRes), permission))
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
    if (Module.is15) {
      addIfMissing(R.string.foreground_service_location, Permissions.FOREGROUND_SERVICE_LOCATION)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      addIfMissing(R.string.post_notification, Permissions.POST_NOTIFICATION)
    }

    return if (permissionItems.isEmpty()) {
      toast(R.string.all_permissions_are_enabled)
      false
    } else {
      true
    }
  }

  private fun showPermissionDialog() {
    if (!loadPermissionItems()) return
    withContext { context ->
      val builder = dialogues.getMaterialDialog(context)
      builder.setTitle(R.string.allow_permission)
      val names = permissionItems.map { it.title }
      builder.setItems(names.toTypedArray()) { dialogInterface, i ->
        dialogInterface.dismiss()
        requestPermission(i)
      }
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
      builder.create().show()
    }
  }

  override fun getTitle(): String = getString(R.string.other)

  private class PermissionItem(val title: String, val permission: String)
}
