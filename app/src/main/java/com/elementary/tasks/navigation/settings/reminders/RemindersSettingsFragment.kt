package com.elementary.tasks.navigation.settings.reminders

import android.os.Bundle
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import kotlinx.android.synthetic.main.fragment_settings_reminders.*

/**
 * Copyright 2018 Nazar Suhovich
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
class RemindersSettingsFragment : BaseSettingsFragment() {

    override fun layoutRes(): Int = R.layout.fragment_settings_reminders

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
            setScroll(it)
        }

        initCompletedPrefs()
    }

    private fun initCompletedPrefs() {
        completedPrefs.setOnClickListener { changeCompleted() }
        completedPrefs.isChecked = prefs.moveCompleted
    }

    private fun changeCompleted() {
        val isChecked = completedPrefs.isChecked
        completedPrefs.isChecked = !isChecked
        prefs.moveCompleted = !isChecked
    }

    override fun getTitle(): String = getString(R.string.reminders_)
}
