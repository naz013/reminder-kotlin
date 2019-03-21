package com.elementary.tasks

import android.content.Context
import com.elementary.tasks.core.utils.ThemeUtil

class QrShareProvider(val themeUtil: ThemeUtil) {

    companion object {
        const val TYPE_REMINDER = "reminder"
        const val CHILD_SHARE = "shared"
        const val INTENT_DATA = "intent_data"

        fun hasQrSupport(): Boolean = false

        fun openImportScreen(context: Context) {
        }

        fun openShareScreen(context: Context, data: String, type: String) {
        }

        fun generateEncryptedData(any: Any): String? {
            return null
        }
    }
}