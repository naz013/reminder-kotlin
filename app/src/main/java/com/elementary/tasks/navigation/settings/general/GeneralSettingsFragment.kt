package com.elementary.tasks.navigation.settings.general

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.SplashScreenActivity
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsGeneralBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import com.elementary.tasks.navigation.settings.general.home.PageIdentifier
import com.elementary.tasks.navigation.settings.general.theme.SelectThemeActivity

class GeneralSettingsFragment : BaseSettingsFragment<FragmentSettingsGeneralBinding>() {

    private var mItemSelect: Int = 0
    private val currentTheme: String
        get() {
            val theme = prefs.appTheme
            val accent = themeUtil.accentNames()[prefs.appThemeColor]
            return when (theme) {
                ThemeUtil.THEME_AUTO -> getString(R.string.auto) + " ($accent)"
                else -> SelectThemeActivity.NAMES[theme - 1] + " ($accent)"
            }
        }

    override fun layoutRes(): Int = R.layout.fragment_settings_general

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }

        initAppTheme()
        init24TimePrefs()
        initLanguagePrefs()
        initHomePage()
        initColsPrefs()
    }

    private fun initColsPrefs() {
        binding.twoColsPrefs.isChecked = prefs.isTwoColsEnabled
        binding.twoColsPrefs.setOnClickListener { changeColsPrefs() }
    }

    private fun changeColsPrefs() {
        val b = binding.twoColsPrefs.isChecked
        prefs.isTwoColsEnabled = !b
        binding.twoColsPrefs.isChecked = !b
    }

    private fun initHomePage() {
        binding.homePrefs.setOnClickListener { showHomePageDialog() }
        showHomePage()
    }

    private fun showHomePage() {
        withContext {
            binding.homePrefs.setDetailText(PageIdentifier.name(it, prefs.homePage))
        }
    }

    private fun showHomePageDialog() {
        withContext { ctx ->
            val builder = dialogues.getMaterialDialog(ctx)
            builder.setCancelable(true)
            builder.setTitle(getString(R.string.start_screen))

            val homePages = PageIdentifier.availablePages(ctx)

            val init = prefs.homePage
            mItemSelect = PageIdentifier.index(ctx, init)
            builder.setSingleChoiceItems(homePages.map { it.name }.toTypedArray(), mItemSelect) { _, which -> mItemSelect = which }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                prefs.homePage = homePages[mItemSelect].key
                dialog.dismiss()
                showHomePage()
            }
            builder.create().show()
        }
    }

    private fun initLanguagePrefs() {
        binding.languagePrefs.setOnClickListener { showLanguageDialog() }
        showLanguage()
    }

    private fun showLanguage() {
        withContext {
            binding.languagePrefs.setDetailText(language.getScreenLocaleName(it))
        }
    }

    private fun showLanguageDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setCancelable(true)
            builder.setTitle(getString(R.string.application_language))
            val init = prefs.appLanguage
            mItemSelect = init
            builder.setSingleChoiceItems(resources.getStringArray(R.array.app_languages), mItemSelect) { _, which -> mItemSelect = which }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                prefs.appLanguage = mItemSelect
                dialog.dismiss()
                if (init != mItemSelect) restartApp()
            }
            builder.create().show()
        }
    }

    private fun init24TimePrefs() {
        binding.time24hourPrefs.setOnClickListener { showTimeFormatDialog() }
        showTimeFormat()
    }

    private fun showTimeFormat() {
        binding.time24hourPrefs.setDetailText(currentFormat())
    }

    private fun currentFormat(): String? {
        return when (prefs.hourFormat) {
            0 -> getString(R.string.default_string)
            1 -> getString(R.string.use_24_hour_format)
            2 -> getString(R.string.use_12_hour_format)
            else -> getString(R.string.default_string)
        }
    }

    private fun showTimeFormatDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setCancelable(true)
            builder.setTitle(getString(R.string._24_hour_time_format))

            mItemSelect = prefs.hourFormat
            builder.setSingleChoiceItems(arrayOf(
                    getString(R.string.default_string),
                    getString(R.string.use_24_hour_format),
                    getString(R.string.use_12_hour_format)
            ), mItemSelect) { _, which -> mItemSelect = which }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                prefs.hourFormat = mItemSelect
                dialog.dismiss()
                showTimeFormat()
            }
            builder.create().show()
        }
    }

    private fun initAppTheme() {
        binding.appThemePrefs.setDetailText(currentTheme)
        binding.appThemePrefs.setOnClickListener { selectTheme() }
    }

    override fun getTitle(): String = getString(R.string.general)

    private fun restartApp() {
        startActivity(Intent(context, SplashScreenActivity::class.java))
        activity?.finishAffinity()
    }

    private fun selectTheme() {
        findNavController().navigate(GeneralSettingsFragmentDirections.actionGeneralSettingsFragmentToSelectThemeActivity())
    }
}
