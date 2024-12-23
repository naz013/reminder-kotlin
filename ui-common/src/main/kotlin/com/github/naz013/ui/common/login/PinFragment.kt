package com.github.naz013.ui.common.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.databinding.FragmentLoginPinBinding
import org.koin.android.ext.android.inject

internal class PinFragment : AuthFragment() {

  private val authPreferences by inject<AuthPreferences>()
  private lateinit var binding: FragmentLoginPinBinding
  private var hasFinger = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      hasFinger = it.getBoolean(ARG_HAS_FINGER, false)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentLoginPinBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.closeButton.setOnClickListener {
      requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    binding.pinView.supportFinger = hasFinger
    binding.pinView.shuffleMode = authPreferences.shufflePinView
    binding.pinView.callback = {
      if (it.length == 6) {
        tryLogin(it)
      }
    }
    binding.pinView.fingerprintButtonCallback = { authCallback?.changeScreen(AUTH_FINGER) }
  }

  private fun tryLogin(pin: String) {
    if (pin.length < 6) {
      Toast.makeText(context, R.string.wrong_pin, Toast.LENGTH_SHORT).show()
      binding.pinView.clearPin()
      return
    }

    if (pin == authPreferences.pinCode) {
      authCallback?.onSuccess()
    } else {
      Toast.makeText(context, R.string.pin_not_match, Toast.LENGTH_SHORT).show()
      binding.pinView.clearPin()
    }
  }

  companion object {
    private const val ARG_HAS_FINGER = "arg_has_finger"

    fun newInstance(hasFinger: Boolean = false): PinFragment {
      val fragment = PinFragment()
      val bundle = Bundle()
      bundle.putBoolean(ARG_HAS_FINGER, hasFinger)
      fragment.arguments = bundle
      return fragment
    }
  }
}
