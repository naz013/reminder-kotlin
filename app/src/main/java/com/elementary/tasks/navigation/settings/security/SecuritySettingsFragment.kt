package com.elementary.tasks.navigation.settings.security

import android.content.Context
import android.os.Bundle
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.FingerInitializer
import com.elementary.tasks.core.utils.FingerprintHelper
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsSecurityBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

/**
 * Copyright 2018 Nazar Suhovich
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
class SecuritySettingsFragment : BaseSettingsFragment<FragmentSettingsSecurityBinding>() {

    override fun layoutRes(): Int = R.layout.fragment_settings_security

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }

        binding.changePinPrefs.setDependentView(binding.pinSwitchPrefs)
        binding.changePinPrefs.setOnClickListener {
            callback?.openFragment(ChangePinFragment(), getString(R.string.change_pin))
        }

        initFingerPrefs()
        initPhonePrefs()
    }

    private fun initPhonePrefs() {
        if (Module.hasTelephony(context!!)) {
            binding.telephonyPrefs.isEnabled = true
            binding.telephonyPrefs.setOnClickListener { changePhonePrefs() }
            binding.telephonyPrefs.isChecked = prefs.isTelephonyEnabled
        } else {
            prefs.isTelephonyEnabled = false
            binding.telephonyPrefs.isChecked = false
            binding.telephonyPrefs.isEnabled = false
        }
    }

    private fun changePhonePrefs() {
        val isChecked = binding.telephonyPrefs.isChecked
        binding.telephonyPrefs.isChecked = !isChecked
        prefs.isTelephonyEnabled = !isChecked
    }

    private fun initPinPrefs() {
        binding.pinSwitchPrefs.setOnClickListener { changePinPrefs() }
        binding.pinSwitchPrefs.isChecked = prefs.hasPinCode
    }

    private fun changePinPrefs() {
        val isChecked = binding.pinSwitchPrefs.isChecked
        if (isChecked) {
            callback?.openFragment(DisablePinFragment(), getString(R.string.disable_pin))
        } else {
            callback?.openFragment(AddPinFragment(), getString(R.string.add_pin))
        }
    }

    private fun initFingerPrefs() {
        binding.fingerprintSwitchPrefs.setOnClickListener { changeFingerPrefs() }
        binding.fingerprintSwitchPrefs.setDependentView(binding.pinSwitchPrefs)
        binding.fingerprintSwitchPrefs.isChecked = prefs.useFingerprint

        FingerInitializer(context!!, null, object : FingerInitializer.ReadyListener {
            override fun onFailToCreate() {
                binding.fingerprintSwitchPrefs.visibility = View.GONE
            }

            override fun onReady(context: Context, fingerprintUiHelper: FingerprintHelper) {
                if (fingerprintUiHelper.canUseFinger(context)) {
                    binding.fingerprintSwitchPrefs.visibility = View.VISIBLE
                } else {
                    binding.fingerprintSwitchPrefs.visibility = View.GONE
                }
            }
        })
    }

    private fun changeFingerPrefs() {
        val isChecked = binding.fingerprintSwitchPrefs.isChecked
        binding.fingerprintSwitchPrefs.isChecked = !isChecked
        prefs.useFingerprint = !isChecked
    }

    override fun onBackStackResume() {
        super.onBackStackResume()
        initPinPrefs()
    }

    override fun getTitle(): String = getString(R.string.security)
}
