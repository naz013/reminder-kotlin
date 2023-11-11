package com.elementary.tasks.settings.other

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.databinding.DialogAboutBinding
import com.elementary.tasks.databinding.FragmentSettingsOtherBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import org.koin.android.ext.android.inject

class OtherSettingsFragment : BaseSettingsFragment<FragmentSettingsOtherBinding>() {

  private val packageManagerWrapper by inject<PackageManagerWrapper>()

  private val mDataList = ArrayList<Item>()
  private val translators: String
    get() {
      val list = listOf(*resources.getStringArray(R.array.app_translators))
      val sb = StringBuilder()
      for (s in list) sb.append(s).append("\n")
      return sb.toString()
    }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsOtherBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.aboutPrefs.setOnClickListener { showAboutDialog() }
    binding.ossPrefs.setOnClickListener { openOssScreen() }
    binding.permissionsPrefs.setOnClickListener { openPermissionsScreen() }
    binding.changesPrefs.setOnClickListener { openChangesScreen() }
    binding.ratePrefs.setOnClickListener { withContext { SuperUtil.launchMarket(it) } }
    binding.tellFriendsPrefs.setOnClickListener { shareApplication() }
    binding.permissionsPrefs.visibility = View.VISIBLE
    binding.addPermissionPrefs.visibility = View.VISIBLE
    binding.addPermissionPrefs.setOnClickListener { showPermissionDialog() }
    binding.privacyPolicyPrefs.setOnClickListener { openPrivacyPolicyScreen() }
    binding.termsPrefs.setOnClickListener { openTermsScreen() }
    binding.feedbackPrefs.setOnClickListener { openFeedbackScreen() }
  }

  override fun getTitle(): String = getString(R.string.other)

  private fun openFeedbackScreen() {
    safeNavigation {
      OtherSettingsFragmentDirections.actionOtherSettingsFragmentToFeedbackFragment()
    }
  }

  private fun openTermsScreen() {
    safeNavigation {
      OtherSettingsFragmentDirections.actionOtherSettingsFragmentToTermsFragment()
    }
  }

  private fun openPrivacyPolicyScreen() {
    safeNavigation {
      OtherSettingsFragmentDirections.actionOtherSettingsFragmentToPrivacyPolicyFragment()
    }
  }

  private fun requestPermission(position: Int) {
    permissionFlow.askPermission(mDataList[position].permission) {
      showPermissionDialog()
    }
  }

  private fun loadDataToList(): Boolean {
    mDataList.clear()

    val activity = activity ?: return false

    if (!Permissions.checkPermission(activity, Permissions.ACCESS_COARSE_LOCATION)) {
      mDataList.add(Item(getString(R.string.course_location), Permissions.ACCESS_COARSE_LOCATION))
    }
    if (!Permissions.checkPermission(activity, Permissions.ACCESS_FINE_LOCATION)) {
      mDataList.add(Item(getString(R.string.fine_location), Permissions.ACCESS_FINE_LOCATION))
    }
    if (!Permissions.checkPermission(activity, Permissions.CALL_PHONE)) {
      mDataList.add(Item(getString(R.string.call_phone), Permissions.CALL_PHONE))
    }
    if (!Permissions.checkPermission(activity, Permissions.GET_ACCOUNTS)) {
      mDataList.add(Item(getString(R.string.get_accounts), Permissions.GET_ACCOUNTS))
    }
    if (!Permissions.checkPermission(activity, Permissions.READ_CALENDAR)) {
      mDataList.add(Item(getString(R.string.read_calendar), Permissions.READ_CALENDAR))
    }
    if (!Permissions.checkPermission(activity, Permissions.WRITE_CALENDAR)) {
      mDataList.add(Item(getString(R.string.write_calendar), Permissions.WRITE_CALENDAR))
    }
    if (!Permissions.checkPermission(activity, Permissions.READ_CONTACTS)) {
      mDataList.add(Item(getString(R.string.read_contacts), Permissions.READ_CONTACTS))
    }
    if (!Permissions.checkPermission(activity, Permissions.READ_EXTERNAL)) {
      mDataList.add(Item(getString(R.string.read_external_storage), Permissions.READ_EXTERNAL))
    }
    if (!Permissions.checkPermission(activity, Permissions.WRITE_EXTERNAL)) {
      mDataList.add(Item(getString(R.string.write_external_storage), Permissions.WRITE_EXTERNAL))
    }
    if (!Permissions.checkPermission(activity, Permissions.RECORD_AUDIO)) {
      mDataList.add(Item(getString(R.string.record_audio), Permissions.RECORD_AUDIO))
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      if (!Permissions.checkPermission(activity, Permissions.FOREGROUND_SERVICE)) {
        mDataList.add(Item(getString(R.string.foreground_service), Permissions.FOREGROUND_SERVICE))
      }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      if (!Permissions.checkPermission(activity, Permissions.BACKGROUND_LOCATION)) {
        mDataList.add(
          Item(getString(R.string.background_location), Permissions.BACKGROUND_LOCATION)
        )
      }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (!Permissions.checkPermission(activity, Permissions.POST_NOTIFICATION)) {
        mDataList.add(Item(getString(R.string.post_notification), Permissions.POST_NOTIFICATION))
      }
    }
    return if (mDataList.size == 0) {
      toast(R.string.all_permissions_are_enabled)
      false
    } else {
      true
    }
  }

  private fun shareApplication() {
    withContext {
      val shareIntent = Intent(Intent.ACTION_SEND)
      shareIntent.type = "text/plain"
      shareIntent.putExtra(
        Intent.EXTRA_TEXT,
        "https://play.google.com/store/apps/details?id=" + it.packageName
      )
      context?.startActivity(Intent.createChooser(shareIntent, "Share..."))
    }
  }

  private fun openChangesScreen() {
    safeNavigation {
      OtherSettingsFragmentDirections.actionOtherSettingsFragmentToChangesFragment()
    }
  }

  private fun openPermissionsScreen() {
    safeNavigation {
      OtherSettingsFragmentDirections.actionOtherSettingsFragmentToPermissionsFragment()
    }
  }

  private fun openOssScreen() {
    safeNavigation {
      OtherSettingsFragmentDirections.actionOtherSettingsFragmentToOssFragment()
    }
  }

  private fun showPermissionDialog() {
    if (!loadDataToList()) return
    withContext { context ->
      val builder = dialogues.getMaterialDialog(context)
      builder.setTitle(R.string.allow_permission)
      val names = mDataList.map { it.title }
      builder.setItems(names.toTypedArray()) { dialogInterface, i ->
        dialogInterface.dismiss()
        requestPermission(i)
      }
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun showAboutDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      val b = DialogAboutBinding.inflate(LayoutInflater.from(it))
      val name: String =
        if (Module.isPro) getString(R.string.app_name_pro) else getString(R.string.app_name)
      b.appName.text = name.uppercase()
      b.translatorsList.text = translators
      b.appVersion.text = packageManagerWrapper.getVersionName()
      builder.setView(b.root)
      builder.create().show()
    }
  }

  internal class Item(val title: String, val permission: String)
}
