package com.elementary.tasks.navigation.settings

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.databinding.DialogAboutLayoutBinding
import com.elementary.tasks.databinding.FragmentSettingsOtherBinding
import com.elementary.tasks.navigation.settings.other.ChangesFragment
import com.elementary.tasks.navigation.settings.other.OssFragment
import com.elementary.tasks.navigation.settings.other.PermissionsFragment

import java.util.ArrayList
import java.util.Arrays

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class OtherSettingsFragment : BaseSettingsFragment() {

    private val mDataList = ArrayList<Item>()

    private val mAboutClick = { view -> showAboutDialog() }
    private val mOssClick = { view -> openOssScreen() }
    private val mPermissionsClick = { view -> openPermissionsScreen() }
    private val mChangesClick = { view -> openChangesScreen() }
    private val mRateClick = { view -> SuperUtil.launchMarket(context!!) }
    private val mShareClick = { view -> shareApplication() }
    private val mAddClick = { view -> showPermissionDialog() }

    private val translators: String
        get() {
            val list = Arrays.asList(*resources.getStringArray(R.array.app_translators))
            val sb = StringBuilder()
            for (s in list) sb.append(s).append("\n")
            return sb.toString()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentSettingsOtherBinding.inflate(inflater, container, false)
        binding.aboutPrefs.setOnClickListener(mAboutClick)
        binding.ossPrefs.setOnClickListener(mOssClick)
        binding.permissionsPrefs.setOnClickListener(mPermissionsClick)
        binding.changesPrefs.setOnClickListener(mChangesClick)
        binding.ratePrefs.setOnClickListener(mRateClick)
        binding.tellFriendsPrefs.setOnClickListener(mShareClick)
        if (Module.isMarshmallow) {
            binding.permissionsPrefs.visibility = View.VISIBLE
            binding.addPermissionPrefs.visibility = View.VISIBLE
        } else {
            binding.permissionsPrefs.visibility = View.GONE
            binding.addPermissionPrefs.visibility = View.GONE
        }
        binding.addPermissionPrefs.setOnClickListener(mAddClick)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.other))
            callback!!.onFragmentSelect(this)
        }
    }

    private fun requestPermission(position: Int) {
        Permissions.requestPermission(activity, position, mDataList[position].permission)
    }

    private fun loadDataToList(): Boolean {
        mDataList.clear()
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
        if (!Permissions.checkPermission(activity, Permissions.READ_CALLS)) {
            mDataList.add(Item(getString(R.string.call_history), Permissions.READ_CALLS))
        }
        if (!Permissions.checkPermission(activity, Permissions.READ_EXTERNAL)) {
            mDataList.add(Item(getString(R.string.read_external_storage), Permissions.READ_EXTERNAL))
        }
        if (!Permissions.checkPermission(activity, Permissions.WRITE_EXTERNAL)) {
            mDataList.add(Item(getString(R.string.write_external_storage), Permissions.WRITE_EXTERNAL))
        }
        if (!Permissions.checkPermission(activity, Permissions.SEND_SMS)) {
            mDataList.add(Item(getString(R.string.send_sms), Permissions.SEND_SMS))
        }
        if (mDataList.size == 0) {
            Toast.makeText(context, R.string.all_permissions_are_enabled, Toast.LENGTH_SHORT).show()
            return false
        } else {
            return true
        }
    }

    private fun shareApplication() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + context!!.packageName)
        context!!.startActivity(Intent.createChooser(shareIntent, "Share..."))
    }

    private fun openChangesScreen() {
        replaceFragment(ChangesFragment(), getString(R.string.changes))
    }

    private fun openPermissionsScreen() {
        replaceFragment(PermissionsFragment(), getString(R.string.permissions))
    }

    private fun openOssScreen() {
        replaceFragment(OssFragment(), getString(R.string.open_source_licenses))
    }

    private fun showPermissionDialog() {
        if (!loadDataToList()) return
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.allow_permission)
        builder.setSingleChoiceItems(object : ArrayAdapter<Item>(context!!, android.R.layout.simple_list_item_1) {
            override fun getCount(): Int {
                return mDataList.size
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var convertView = convertView
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
                }
                val tvName = convertView!!.findViewById<TextView>(android.R.id.text1)
                tvName.text = mDataList[position].title
                return convertView
            }
        }, -1) { dialogInterface, i ->
            dialogInterface.dismiss()
            requestPermission(i)
        }
        builder.create().show()
    }

    private fun showAboutDialog() {
        val builder = Dialogues.getDialog(context!!)
        val binding = DialogAboutLayoutBinding.inflate(LayoutInflater.from(context))
        val name: String
        if (Module.isPro)
            name = getString(R.string.app_name_pro)
        else
            name = getString(R.string.app_name)
        binding.appName.text = name.toUpperCase()
        binding.translatorsList.text = translators
        val pInfo: PackageInfo
        try {
            pInfo = context!!.packageManager.getPackageInfo(context!!.packageName, 0)
            binding.appVersion.text = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        builder.setView(binding.root)
        builder.create().show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog()
        }
    }

    internal class Item(val title: String, val permission: String)
}
