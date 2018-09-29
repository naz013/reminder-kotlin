package com.elementary.tasks.navigation.settings

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.navigation.settings.voice.HelpFragment
import com.elementary.tasks.navigation.settings.voice.TimeOfDayFragment
import kotlinx.android.synthetic.main.fragment_settings_voice.*

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

class VoiceSettingsFragment : BaseSettingsFragment() {

    override fun layoutRes(): Int = R.layout.fragment_settings_voice

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
            callback?.onScrollUpdate(it)
        }
        initLanguagePrefs()
        timePrefs.setOnClickListener { replaceFragment(TimeOfDayFragment(), getString(R.string.time)) }
        helpPrefs.setOnClickListener { replaceFragment(HelpFragment(), getString(R.string.help)) }
        initConversationPrefs()
    }

    private fun initLanguagePrefs() {
        languagePrefs.setOnClickListener { showLanguageDialog() }
        showLanguage()
    }

    private fun showLanguage() {
        languagePrefs.setDetailText(language.getLanguages(context!!)[prefs.voiceLocale])
    }

    private fun initConversationPrefs() {
        conversationPrefs.setOnClickListener { changeLivePrefs() }
        conversationPrefs.isChecked = prefs.isLiveEnabled
    }

    private fun changeLivePrefs() {
        val isChecked = conversationPrefs.isChecked
        prefs.isLiveEnabled = !isChecked
        conversationPrefs.isChecked = !isChecked
    }

    override fun onResume() {
        super.onResume()
        callback?.onTitleChange(getString(R.string.voice_control))
        callback?.onFragmentSelect(this)
    }

    private fun showLanguageDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.language))
        val locales = language.getLanguages(context!!)
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, locales)
        val language = prefs.voiceLocale
        builder.setSingleChoiceItems(adapter, language) { _, which ->
            if (which != -1) {
                prefs.voiceLocale = which
            }
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setOnDismissListener { showLanguage() }
        dialog.show()
    }
}
