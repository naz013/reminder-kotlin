package com.elementary.tasks.settings.general

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentSettingsGeneralBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.elementary.tasks.splash.SplashScreenActivity
import com.github.naz013.common.Module
import com.github.naz013.ui.common.activity.finishWith
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.visible
import com.github.naz013.ui.common.view.visibleGone
import com.google.android.material.color.DynamicColors

class GeneralSettingsFragment : BaseSettingsFragment<FragmentSettingsGeneralBinding>() {

  private var mItemSelect: Int = 0

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsGeneralBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initAppTheme()
    init24TimePrefs()
    initLanguagePrefs()
    initDynamicColorsPrefs()
    initAnalyticsPrefs()
    initUnitPrefs()

    binding.themePreviewPrefs.visibleGone(BuildConfig.DEBUG)
    binding.themePreviewPrefs.setOnClickListener {
      safeNavigation {
        GeneralSettingsFragmentDirections.actionGeneralSettingsFragmentToUiPreviewFragment()
      }
    }
  }

  private fun initUnitPrefs() {
    binding.unitsPrefs.setOnClickListener { changeUnitPrefs() }
    binding.unitsPrefs.isChecked = prefs.useMetric
  }

  private fun changeUnitPrefs() {
    prefs.useMetric = !prefs.useMetric
    binding.unitsPrefs.isChecked = prefs.useMetric
  }

  private fun initAnalyticsPrefs() {
    binding.analyticsPrefs.setOnClickListener { changeAnalyticsPrefs() }
    binding.analyticsPrefs.isChecked = prefs.analyticsEnabled
  }

  private fun changeAnalyticsPrefs() {
    prefs.analyticsEnabled = !prefs.analyticsEnabled
    binding.analyticsPrefs.isChecked = prefs.analyticsEnabled
  }

  private fun initDynamicColorsPrefs() {
    if (Module.is12) {
      binding.dynamicColorsPrefs.visible()
      binding.dynamicColorsPrefs.setOnClickListener { changeDynamicPrefs() }
      binding.dynamicColorsPrefs.isChecked = prefs.useDynamicColors
    } else {
      binding.dynamicColorsPrefs.gone()
    }
  }

  private fun changeDynamicPrefs() {
    prefs.useDynamicColors = !prefs.useDynamicColors
    binding.dynamicColorsPrefs.isChecked = prefs.useDynamicColors
    withActivity {
      if (prefs.useDynamicColors) {
        DynamicColors.applyToActivityIfAvailable(it)
      }
      it.recreate()
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
      builder.setSingleChoiceItems(
        resources.getStringArray(R.array.app_languages),
        mItemSelect
      ) { _, which -> mItemSelect = which }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        prefs.appLanguage = mItemSelect
        dialog.dismiss()
        if (init != mItemSelect) restartApp()
      }
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
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

  private fun currentFormat(): String {
    return when (prefs.hourFormat) {
      0 -> getString(R.string.system_default)
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
      builder.setSingleChoiceItems(
        arrayOf(
          getString(R.string.system_default),
          getString(R.string.use_24_hour_format),
          getString(R.string.use_12_hour_format)
        ),
        mItemSelect
      ) { _, which -> mItemSelect = which }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        prefs.hourFormat = mItemSelect
        dialog.dismiss()
        showTimeFormat()
      }
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun initAppTheme() {
    binding.appThemePrefs.setDetailText(themeNames()[getThemeIndex(prefs.nightMode)])
    binding.appThemePrefs.setOnClickListener {
      showThemeDialog()
    }
  }

  private fun showThemeDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setCancelable(true)
      builder.setTitle(getString(R.string.theme))
      mItemSelect = getThemeIndex(prefs.nightMode)
      builder.setSingleChoiceItems(themeNames(), mItemSelect) { _, which -> mItemSelect = which }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        prefs.nightMode = getTheme(mItemSelect)
        dialog.dismiss()
        activity?.recreate()
      }
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun themeNames(): Array<String> {
    return arrayOf(
      getString(R.string.light),
      getString(R.string.dark),
      getString(R.string.system_default)
    )
  }

  private fun getTheme(index: Int): Int {
    return when (index) {
      0 -> AppCompatDelegate.MODE_NIGHT_NO
      1 -> AppCompatDelegate.MODE_NIGHT_YES
      else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
  }

  private fun getThemeIndex(theme: Int): Int {
    return when (theme) {
      AppCompatDelegate.MODE_NIGHT_NO -> 0
      AppCompatDelegate.MODE_NIGHT_YES -> 1
      else -> 2
    }
  }

  override fun getTitle(): String = getString(R.string.general)

  private fun restartApp() {
    activity?.finishWith(SplashScreenActivity::class.java)
  }
}
