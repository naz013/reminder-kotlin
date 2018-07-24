package com.elementary.tasks.navigation.fragments

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Constants
import java.util.*

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

class HelpFragment : BaseWebViewFragment() {

    override val url: String
        get() {
            val isDark = themeUtil.isDark
            val localeCheck = Locale.getDefault().toString().toLowerCase()
            val url: String
            when {
                localeCheck.startsWith("uk") -> url = if (isDark) {
                    Constants.WEB_URL + "app_help/index.html"
                } else {
                    Constants.WEB_URL + "app_help/index_light.html"
                }
                localeCheck.startsWith("ru") -> url = if (isDark) {
                    Constants.WEB_URL + "app_help/index_ru.html"
                } else {
                    Constants.WEB_URL + "app_help/index_light_ru.html"
                }
                else -> url = if (isDark) {
                    Constants.WEB_URL + "app_help/index_en.html"
                } else {
                    Constants.WEB_URL + "app_help/index_light_en.html"
                }
            }
            return url
        }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback?.onTitleChange(getString(R.string.help))
            callback?.onFragmentSelect(this)
        }
    }
}
