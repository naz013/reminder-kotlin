package com.elementary.tasks.navigation.settings.calendar

import com.elementary.tasks.R

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
class FragmentBirthdaysColor : FragmentStyle() {

    override val selectedColor: Int
        get() = prefs.birthdayColor

    override fun saveToPrefs(code: Int) {
        prefs.birthdayColor = code
    }

    override fun getTitle(): String = getString(R.string.birthdays_color)
}
