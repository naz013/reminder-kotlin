package com.elementary.tasks.settings.other

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.DialogAboutBinding
import com.elementary.tasks.databinding.FragmentSettingsOtherBinding
import com.elementary.tasks.settings.BaseSettingsFragment

class OtherSettingsFragment : BaseSettingsFragment<FragmentSettingsOtherBinding>() {

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
    ViewUtils.listenScrollableView(binding.scrollView) {
      setToolbarAlpha(toAlpha(it.toFloat(), NESTED_SCROLL_MAX))
    }

    binding.aboutPrefs.setOnClickListener { showAboutDialog() }
    binding.ossPrefs.setOnClickListener { openOssScreen() }
    binding.permissionsPrefs.setOnClickListener { openPermissionsScreen() }
    binding.changesPrefs.setOnClickListener { openChangesScreen() }
    binding.ratePrefs.setOnClickListener { withContext { SuperUtil.launchMarket(it) } }
    binding.tellFriendsPrefs.setOnClickListener { shareApplication() }
    if (Module.isMarshmallow) {
      binding.permissionsPrefs.visibility = View.VISIBLE
      binding.addPermissionPrefs.visibility = View.VISIBLE
    } else {
      binding.permissionsPrefs.visibility = View.GONE
      binding.addPermissionPrefs.visibility = View.GONE
    }
    binding.addPermissionPrefs.setOnClickListener { showPermissionDialog() }
    binding.privacyPolicyPrefs.setOnClickListener { openPrivacyPolicyScreen() }
  }

  override fun getTitle(): String = getString(R.string.other)

  private fun openPrivacyPolicyScreen() {
    navigate {
      OtherSettingsFragmentDirections.actionOtherSettingsFragmentToPrivacyPolicyFragment()
    }
  }

  private fun requestPermission(position: Int) {
    withActivity {
      Permissions.requestPermission(it, position, mDataList[position].permission)
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
    if (!Permissions.checkPermission(activity, Permissions.READ_PHONE_STATE)) {
      mDataList.add(Item(getString(R.string.read_phone_state), Permissions.READ_PHONE_STATE))
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
    return if (mDataList.size == 0) {
      Toast.makeText(context, R.string.all_permissions_are_enabled, Toast.LENGTH_SHORT).show()
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
    safeNavigation(OtherSettingsFragmentDirections.actionOtherSettingsFragmentToChangesFragment())
  }

  private fun openPermissionsScreen() {
    safeNavigation(OtherSettingsFragmentDirections.actionOtherSettingsFragmentToPermissionsFragment())
  }

  private fun openOssScreen() {
    safeNavigation(OtherSettingsFragmentDirections.actionOtherSettingsFragmentToOssFragment())
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
      val pInfo: PackageInfo
      try {
        pInfo = it.packageManager.getPackageInfo(it.packageName, 0)
        b.appVersion.text = pInfo.versionName
      } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
      }

      builder.setView(b.root)
      builder.create().show()
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (grantResults.isEmpty()) return
    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      showPermissionDialog()
    }
  }

  internal class Item(val title: String, val permission: String)
}
