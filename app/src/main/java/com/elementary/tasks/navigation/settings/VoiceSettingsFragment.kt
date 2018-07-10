package com.elementary.tasks.navigation.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.databinding.FragmentSettingsVoiceBinding
import com.elementary.tasks.navigation.settings.voice.HelpFragment
import com.elementary.tasks.navigation.settings.voice.TimeOfDayFragment

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

    private var binding: FragmentSettingsVoiceBinding? = null

    private val mVoiceClick = { view -> showLanguageDialog() }
    private val mTimeClick = { view -> replaceFragment(TimeOfDayFragment(), getString(R.string.time)) }
    private val mHelpClick = { view -> replaceFragment(HelpFragment(), getString(R.string.help)) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsVoiceBinding.inflate(inflater, container, false)
        initLanguagePrefs()
        initTimePrefs()
        binding!!.helpPrefs.setOnClickListener(mHelpClick)
        initConversationPrefs()
        return binding!!.root
    }

    private fun initTimePrefs() {
        binding!!.timePrefs.setOnClickListener(mTimeClick)
    }

    private fun initLanguagePrefs() {
        binding!!.languagePrefs.setOnClickListener(mVoiceClick)
        showLanguage()
    }

    private fun showLanguage() {
        binding!!.languagePrefs.setDetailText(Language.getLanguages(context!!)[prefs!!.voiceLocale])
    }

    private fun initConversationPrefs() {
        binding!!.conversationPrefs.setOnClickListener { view -> changeLivePrefs() }
        binding!!.conversationPrefs.isChecked = prefs!!.isLiveEnabled
    }

    private fun changeLivePrefs() {
        val isChecked = binding!!.conversationPrefs.isChecked
        prefs!!.isLiveEnabled = !isChecked
        binding!!.conversationPrefs.isChecked = !isChecked
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.voice_control))
            callback!!.onFragmentSelect(this)
        }
    }

    private fun showLanguageDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.language))
        val locales = Language.getLanguages(context!!)
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, locales)
        val language = prefs!!.voiceLocale
        builder.setSingleChoiceItems(adapter, language) { dialog, which ->
            if (which != -1) {
                prefs!!.voiceLocale = which
            }
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setOnDismissListener { dialogInterface -> showLanguage() }
        dialog.show()
    }
}
