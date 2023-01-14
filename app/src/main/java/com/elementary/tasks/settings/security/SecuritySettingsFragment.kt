package com.elementary.tasks.settings.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsSecurityBinding
import com.elementary.tasks.settings.BaseSettingsFragment

class SecuritySettingsFragment : BaseSettingsFragment<FragmentSettingsSecurityBinding>() {

  private lateinit var biometricPrompt: BiometricPrompt

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

    biometricPrompt = createBiometricPrompt()

    initFingerPrefs()
    initPhonePrefs()
  }

  private fun createBiometricPrompt(): BiometricPrompt {
    val executor = ContextCompat.getMainExecutor(requireContext())
    val callback = object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        setFinger(!binding.fingerprintSwitchPrefs.isChecked)
      }
    }

    return BiometricPrompt(this, executor, callback)
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
    binding.fingerprintSwitchPrefs.setOnClickListener { changeFingerPrefs() }
    binding.fingerprintSwitchPrefs.setDependentView(binding.pinSwitchPrefs)
    binding.fingerprintSwitchPrefs.isChecked = prefs.useFingerprint

    withContext {
      if (BiometricManager.from(it).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
        binding.fingerprintSwitchPrefs.visibility = View.VISIBLE
      } else {
        binding.fingerprintSwitchPrefs.visibility = View.GONE
      }
    }
  }

  private fun setFinger(enabled: Boolean) {
    binding.fingerprintSwitchPrefs.isChecked = enabled
    prefs.useFingerprint = enabled
  }

  private fun changeFingerPrefs() {
    withContext {
      if (BiometricManager.from(it).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
        val promptInfo = createPromptInfo()
        biometricPrompt.authenticate(promptInfo)
      }
    }
  }

  private fun createPromptInfo(): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo.Builder()
      .setTitle(getString(R.string.app_title))
      .setSubtitle(getString(R.string.prompt_info_subtitle))
      .setDescription(getString(R.string.prompt_info_description))
      // Authenticate without requiring the user to press a "confirm"
      // button after satisfying the biometric check
      .setConfirmationRequired(false)
      .setNegativeButtonText(getString(R.string.cancel))
      .build()
  }

  override fun onBackStackResume() {
    super.onBackStackResume()
    initPinPrefs()
  }

  override fun getTitle(): String = getString(R.string.security)
}
