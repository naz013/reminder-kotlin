package com.elementary.tasks.core.utils

import android.content.Context

class FingerInitializer(context: Context, callback: FingerprintHelper.Callback?, listener: ReadyListener) {

    init {
        if (Module.isMarshmallow) {
            if (Module.hasFingerprint(context)) {
                try {
                    listener.onReady(context, FingerprintHelper(context, callback))
                } catch (e: Exception) {
                    listener.onFailToCreate()
                }
            } else {
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