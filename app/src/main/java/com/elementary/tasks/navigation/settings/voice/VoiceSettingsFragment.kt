package com.elementary.tasks.navigation.settings.voice

import android.os.Bundle
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsVoiceBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

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
class VoiceSettingsFragment : BaseSettingsFragment<FragmentSettingsVoiceBinding>() {

    private var mItemSelect: Int = 0
    override fun layoutRes(): Int = R.layout.fragment_settings_voice

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }
        initLanguagePrefs()
        binding.timePrefs.setOnClickListener { callback?.openFragment(TimeOfDayFragment(), getString(R.string.time)) }
        binding.helpPrefs.setOnClickListener { callback?.openFragment(HelpFragment(), getString(R.string.help)) }
        initConversationPrefs()
    }

    private fun initLanguagePrefs() {
        binding.languagePrefs.setOnClickListener { showLanguageDialog() }
        showLanguage()
    }

    private fun showLanguage() {
        withContext {
            binding.languagePrefs.setDetailText(language.getLanguages(it)[prefs.voiceLocale])
        }
    }

    private fun initConversationPrefs() {
        binding.conversationPrefs.setOnClickListener { changeLivePrefs() }
        binding.conversationPrefs.isChecked = prefs.isLiveEnabled
    }

    private fun changeLivePrefs() {
        val isChecked = binding.conversationPrefs.isChecked
        prefs.isLiveEnabled = !isChecked
        binding.conversationPrefs.isChecked = !isChecked
    }

    override fun getTitle(): String = getString(R.string.voice_control)

    private fun showLanguageDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setTitle(getString(R.string.language))
            val locales = language.getLanguages(it).toTypedArray()
            mItemSelect = prefs.voiceLocale
            builder.setSingleChoiceItems(locales, mItemSelect) { _, which ->
                mItemSelect = which
            }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                prefs.voiceLocale = mItemSelect
                showLanguage()
                dialog.dismiss()
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ ->
                showLanguage()
                dialog.dismiss()
            }
            builder.create().show()
        }
    }
}
