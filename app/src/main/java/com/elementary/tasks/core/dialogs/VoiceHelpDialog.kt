package com.elementary.tasks.core.dialogs

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Constants
import java.util.*

/**
 * Copyright 2017 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class VoiceHelpDialog : BaseDialog() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alert = dialogues.getDialog(this)
        alert.setTitle(getString(R.string.help))
        val wv = WebView(this)
        val localeCheck = Locale.getDefault().toString().toLowerCase()
        val url = when {
            localeCheck.startsWith("uk") -> Constants.WEB_URL + "reminder-voice-ukrainian"
            localeCheck.startsWith("ru") -> Constants.WEB_URL + "reminder-voice-russian"
            else -> Constants.WEB_URL + "reminder-voice-english"
        }
        wv.loadUrl(url)
        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
        alert.setView(wv)
        alert.setCancelable(true)
        alert.setNegativeButton(R.string.ok) { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        val alertDialog = alert.create()
        alertDialog.setOnCancelListener { finish() }
        alertDialog.setOnDismissListener { finish() }
        alertDialog.show()
    }
}
