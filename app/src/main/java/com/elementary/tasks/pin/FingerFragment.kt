package com.elementary.tasks.pin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.FingerInitializer
import com.elementary.tasks.core.utils.FingerprintHelper
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.core.utils.transparent
import com.elementary.tasks.databinding.FragmentLoginFingerBinding

class FingerFragment : AuthFragment(), FingerInitializer.ReadyListener, FingerprintHelper.Callback {

    private lateinit var binding: FragmentLoginFingerBinding
    private var fingerprintHelper: FingerprintHelper? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoginFingerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pinButton.setOnClickListener {
            authCallback?.changeScreen(AUTH_PIN)
        }
        FingerInitializer(context!!, this, this)
    }

    override fun onReady(context: Context, fingerprintUiHelper: FingerprintHelper) {
        this.fingerprintHelper = fingerprintUiHelper
        if (fingerprintUiHelper.canUseFinger(context)) {
            binding.statusBg.setBackgroundResource(R.drawable.finger_status_idle_semi)
            binding.statusIcon.setBackgroundResource(R.drawable.finger_status_idle)
            binding.errorView.transparent()
            fingerprintUiHelper.startListening(null)
        } else {
            binding.statusBg.setBackgroundResource(R.drawable.finger_status_error_semi)
            binding.statusIcon.setBackgroundResource(R.drawable.finger_status_error)
            authCallback?.changeScreen(AUTH_PIN)
        }
    }

    override fun onFailToCreate() {
        binding.statusBg.setBackgroundResource(R.drawable.finger_status_error_semi)
        binding.statusIcon.setBackgroundResource(R.drawable.finger_status_error)
        authCallback?.changeScreen(AUTH_PIN)
    }

    override fun onIdle() {
        binding.statusBg.setBackgroundResource(R.drawable.finger_status_idle_semi)
        binding.statusIcon.setBackgroundResource(R.drawable.finger_status_idle)
        binding.errorView.transparent()
    }

    override fun onAuthenticated() {
        binding.errorView.transparent()
        binding.statusBg.setBackgroundResource(R.drawable.finger_status_success_semi)
        binding.statusIcon.setBackgroundResource(R.drawable.finger_status_success)
        authCallback?.onSuccess()
    }

    override fun onError() {
        binding.statusBg.setBackgroundResource(R.drawable.finger_status_error_semi)
        binding.statusIcon.setBackgroundResource(R.drawable.finger_status_error)
        binding.errorView.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        fingerprintHelper?.stopListening()
    }

    companion object {

        fun newInstance(): FingerFragment {
            return FingerFragment()
        }
    }
}