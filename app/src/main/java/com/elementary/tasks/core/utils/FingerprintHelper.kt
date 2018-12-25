package com.elementary.tasks.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.hardware.fingerprint.FingerprintManagerCompat

@SuppressLint("NewApi")
class FingerprintHelper(context: Context, callback: Callback?) : FingerprintManager.AuthenticationCallback() {

    private val ERROR_TIMEOUT_MILLIS: Long = 1600

    @Nullable
    private var mFingerprintManager: FingerprintManager? = null
    @Nullable
    private var mCallback: Callback?
    private var mCancellationSignal: CancellationSignal? = null

    private var mSelfCancelled: Boolean = false
    private val mUiHandler = Handler(Looper.getMainLooper())

    init {
        try {
            mFingerprintManager = context.getSystemService(FingerprintManager::class.java)
        } catch (e: NoClassDefFoundError) {
        }
        mCallback = callback
    }

    fun canUseFinger(@NonNull context: Context): Boolean {
        return checkFingerprintCompatibility(context) && isFingerprintAuthAvailable()
    }

    fun checkFingerprintCompatibility(@NonNull context: Context): Boolean {
        return FingerprintManagerCompat.from(context).isHardwareDetected
    }

    fun isFingerprintAuthAvailable(): Boolean {
        return if (mFingerprintManager == null) false
        else mFingerprintManager?.isHardwareDetected ?: false && mFingerprintManager?.hasEnrolledFingerprints() ?: false
    }

    fun startListening(cryptoObject: FingerprintManager.CryptoObject?) {
        if (!isFingerprintAuthAvailable() || mFingerprintManager == null) {
            return
        }
        mCancellationSignal = CancellationSignal()
        mSelfCancelled = false
        // The line below prevents the false positive inspection from Android Studio

        mFingerprintManager?.authenticate(cryptoObject, mCancellationSignal, 0 /* flags */, this, null)
        if (mCallback != null) {
            mCallback?.onIdle()
        }
    }

    fun stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true
            mCancellationSignal?.cancel()
            mCancellationSignal = null
        }
    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
        if (!mSelfCancelled) {
            if (mCallback != null) {
                mCallback?.onError()
                mUiHandler.postDelayed({ mCallback?.onIdle() }, ERROR_TIMEOUT_MILLIS)
            }
        }
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
        if (mCallback != null) {
            mCallback?.onError()
            mUiHandler.postDelayed({ mCallback?.onIdle() }, ERROR_TIMEOUT_MILLIS)
        }
    }

    override fun onAuthenticationFailed() {
        if (mCallback != null) {
            mCallback?.onError()
            mUiHandler.postDelayed({ mCallback?.onIdle() }, ERROR_TIMEOUT_MILLIS)
        }
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        if (mCallback != null) {
            mCallback?.onAuthenticated()
        }
    }

    interface Callback {

        fun onIdle()

        fun onAuthenticated()

        fun onError()
    }
}