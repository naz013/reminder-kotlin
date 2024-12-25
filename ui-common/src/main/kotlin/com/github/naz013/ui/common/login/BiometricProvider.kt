package com.github.naz013.ui.common.login

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.naz013.ui.common.R

typealias OnSuccessListener = () -> Unit

class BiometricProvider private constructor(
  private val promptCreator: PromptCreator,
  private val onSuccessListener: OnSuccessListener
) {

  constructor(
    activity: FragmentActivity,
    onSuccessListener: OnSuccessListener
  ) : this(ActivityCreator(activity), onSuccessListener)

  constructor(
    fragment: Fragment,
    onSuccessListener: OnSuccessListener
  ) : this(FragmentCreator(fragment), onSuccessListener)

  fun hasBiometric(): Boolean {
    return BiometricManager.from(promptCreator.getContext()).canAuthenticate(
      BiometricManager.Authenticators.BIOMETRIC_WEAK
    ) == BiometricManager.BIOMETRIC_SUCCESS
  }

  fun tryToOpenFingerLogin(): Boolean {
    return if (hasBiometric()) {
      createBiometricPrompt().authenticate(createPromptInfo())
      true
    } else {
      false
    }
  }

  private fun createPromptInfo(): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo.Builder()
      .setTitle(promptCreator.getContext().getString(R.string.app_title))
      .setSubtitle(promptCreator.getContext().getString(R.string.prompt_info_subtitle))
      .setDescription(promptCreator.getContext().getString(R.string.prompt_info_description))
      // Authenticate without requiring the user to press a "confirm"
      // button after satisfying the biometric check
      .setConfirmationRequired(false)
      .setNegativeButtonText(promptCreator.getContext().getString(R.string.enter_your_pin))
      .build()
  }

  private fun createBiometricPrompt(): BiometricPrompt {
    val callback = object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        onSuccessListener.invoke()
      }
    }
    return promptCreator.create(callback)
  }

  abstract class PromptCreator {
    abstract fun create(callback: BiometricPrompt.AuthenticationCallback): BiometricPrompt
    abstract fun getContext(): Context
  }

  class ActivityCreator(
    private val activity: FragmentActivity
  ) : PromptCreator() {
    override fun create(callback: BiometricPrompt.AuthenticationCallback): BiometricPrompt {
      return BiometricPrompt(activity, ContextCompat.getMainExecutor(getContext()), callback)
    }

    override fun getContext(): Context {
      return activity.applicationContext
    }
  }

  class FragmentCreator(
    private val fragment: Fragment
  ) : PromptCreator() {
    override fun create(callback: BiometricPrompt.AuthenticationCallback): BiometricPrompt {
      return BiometricPrompt(fragment, ContextCompat.getMainExecutor(getContext()), callback)
    }

    override fun getContext(): Context {
      return fragment.requireContext()
    }
  }
}
