package com.elementary.tasks.navigation.settings.security

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.FingerInitializer
import com.elementary.tasks.core.utils.FingerprintHelper
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsSecurityBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

class SecuritySettingsFragment : BaseSettingsFragment<FragmentSettingsSecurityBinding>() {

    override fun layoutRes(): Int = R.layout.fragment_settings_security

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setToolbarAlpha(toAlpha(it.toFloat(), NESTED_SCROLL_MAX))
        }

        binding.changePinPrefs.setDependentView(binding.pinSwitchPrefs)
        binding.changePinPrefs.setOnClickListener {
            findNavController().navigate(SecuritySettingsFragmentDirections.actionSecuritySettingsFragmentToChangePinFragment())
        }

        initFingerPrefs()
        initPhonePrefs()
    }

    private fun initPhonePrefs() {
        withContext {
            if (Module.hasTelephony(it)) {
                binding.telephonyPrefs.isEnabled = true
                binding.telephonyPrefs.setOnClickListener { changePhonePrefs() }
                binding.telephonyPrefs.isChecked = prefs.isTelephonyEnabled
            } else {
                prefs.isTelephonyEnabled = false
                binding.telephonyPrefs.isChecked = false
                binding.telephonyPrefs.isEnabled = false
            }
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
            findNavController().navigate(SecuritySettingsFragmentDirections.actionSecuritySettingsFragmentToDisablePinFragment())
        } else {
            findNavController().navigate(SecuritySettingsFragmentDirections.actionSecuritySettingsFragmentToAddPinFragment())
        }
    }

    private fun initFingerPrefs() {
        binding.fingerprintSwitchPrefs.setOnClickListener { changeFingerPrefs() }
        binding.fingerprintSwitchPrefs.setDependentView(binding.pinSwitchPrefs)
        binding.fingerprintSwitchPrefs.isChecked = prefs.useFingerprint

        withContext {
            FingerInitializer(it, null, object : FingerInitializer.ReadyListener {
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
