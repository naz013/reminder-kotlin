package com.elementary.tasks.settings.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.os.BiometricProvider
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.FragmentSettingsSecurityBinding
import com.elementary.tasks.settings.BaseSettingsFragment

class SecuritySettingsFragment : BaseSettingsFragment<FragmentSettingsSecurityBinding>() {

  private val biometricProvider = BiometricProvider(this) {
    setFinger(!binding.fingerprintSwitchPrefs.isChecked)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsSecurityBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    ViewUtils.listenScrollableView(binding.scrollView) {
      setToolbarAlpha(toAlpha(it.toFloat(), NESTED_SCROLL_MAX))
    }

    binding.changePinPrefs.setDependentView(binding.pinSwitchPrefs)
    binding.changePinPrefs.setOnClickListener {
      safeNavigation(
        SecuritySettingsFragmentDirections.actionSecuritySettingsFragmentToChangePinFragment()
      )
    }

    initFingerPrefs()
    initShufflePrefs()
    initPhonePrefs()
  }

  private fun initShufflePrefs() {
    binding.shufflePrefs.setOnClickListener { changeShufflePrefs() }
    binding.shufflePrefs.setDependentView(binding.pinSwitchPrefs)
    binding.shufflePrefs.isChecked = prefs.shufflePinView
  }

  private fun changeShufflePrefs() {
    val isChecked = binding.shufflePrefs.isChecked
    binding.shufflePrefs.isChecked = !isChecked
    prefs.shufflePinView = !isChecked
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
      safeNavigation(SecuritySettingsFragmentDirections.actionSecuritySettingsFragmentToDisablePinFragment())
    } else {
      safeNavigation(SecuritySettingsFragmentDirections.actionSecuritySettingsFragmentToAddPinFragment())
    }
  }

  private fun initFingerPrefs() {
    binding.fingerprintSwitchPrefs.setOnClickListener { biometricProvider.tryToOpenFingerLogin() }
    binding.fingerprintSwitchPrefs.setDependentView(binding.pinSwitchPrefs)
    binding.fingerprintSwitchPrefs.isChecked = prefs.useFingerprint
    binding.fingerprintSwitchPrefs.visibleGone(biometricProvider.hasBiometric())
  }

  private fun setFinger(enabled: Boolean) {
    binding.fingerprintSwitchPrefs.isChecked = enabled
    prefs.useFingerprint = enabled
  }

  override fun onBackStackResume() {
    super.onBackStackResume()
    initPinPrefs()
  }

  override fun getTitle(): String = getString(R.string.security)
}
