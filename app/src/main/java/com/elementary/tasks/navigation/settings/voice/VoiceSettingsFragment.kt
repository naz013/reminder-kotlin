package com.elementary.tasks.navigation.settings.voice

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsVoiceBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

class VoiceSettingsFragment : BaseSettingsFragment<FragmentSettingsVoiceBinding>() {

    private var mItemSelect: Int = 0
    override fun layoutRes(): Int = R.layout.fragment_settings_voice

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setToolbarAlpha(toAlpha(it.toFloat(), NESTED_SCROLL_MAX))
        }
        initLanguagePrefs()
        binding.timePrefs.setOnClickListener {
            findNavController().navigate(VoiceSettingsFragmentDirections.actionVoiceSettingsFragmentToTimeOfDayFragment())
        }
        binding.helpPrefs.setOnClickListener {
            findNavController().navigate(VoiceSettingsFragmentDirections.actionVoiceSettingsFragmentToHelpFragment2())
        }
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
