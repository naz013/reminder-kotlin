package com.elementary.tasks.navigation.settings.voice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.databinding.FragmentSettingsWebViewBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

import java.util.Locale

/**
 * Copyright 2016 Nazar Suhovich
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

class HelpFragment : BaseSettingsFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentSettingsWebViewBinding.inflate(inflater, container, false)
        val localeCheck = Locale.getDefault().toString().toLowerCase()
        val url: String
        if (localeCheck.startsWith("uk")) {
            url = Constants.WEB_URL + "voice_help/voice_uk.html"
        } else if (localeCheck.startsWith("ru")) {
            url = Constants.WEB_URL + "voice_help/voice_ru.html"
        } else {
            url = Constants.WEB_URL + "voice_help/voice_en.html"
        }
        binding.webView.loadUrl(url)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.help))
            callback!!.onFragmentSelect(this)
        }
    }
}
