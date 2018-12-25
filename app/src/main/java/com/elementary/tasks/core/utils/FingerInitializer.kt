package com.elementary.tasks.core.utils

import android.content.Context
import android.os.Build

class FingerInitializer(context: Context, callback: FingerprintHelper.Callback?, listener: ReadyListener) {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                listener.onReady(context, FingerprintHelper(context, callback))
            } catch (e: Exception) {
                listener.onFailToCreate()
            }

        } else {
            listener.onFailToCreate()
        }
    }

    interface ReadyListener {
        fun onReady(context: Context, fingerprintUiHelper: FingerprintHelper)

        fun onFailToCreate()
    }
}