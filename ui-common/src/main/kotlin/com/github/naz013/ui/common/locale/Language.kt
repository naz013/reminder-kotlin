package com.github.naz013.ui.common.locale

import android.content.Context
import com.github.naz013.common.TextProvider
import com.github.naz013.ui.common.R
import java.util.Locale

class Language(
  private val localePreferences: LocalePreferences,
  private val context: Context,
  private val textProvider: TextProvider
) {

  fun getCurrentLocale(): String {
    val defLocale = context.resources.configuration.locale
    val locale = runCatching { context.resources.configuration.locales.get(0) }.getOrNull()
      ?: defLocale
    return locale.language
  }

  fun onAttach(context: Context): Context {
    return setLocale(context, getScreenLanguage(localePreferences.appLanguage)).also {
      textProvider.updateContext(it)
    }
  }

  private fun setLocale(context: Context, locale: Locale): Context {
    return updateResources(context, locale)
  }

  private fun updateResources(context: Context, locale: Locale): Context {
    Locale.setDefault(locale)
    val configuration = context.resources.configuration
    configuration.setLocale(locale)
    configuration.setLayoutDirection(locale)
    return context.createConfigurationContext(configuration)
  }

  fun getScreenLocaleName(context: Context): String =
    context.resources.getStringArray(R.array.app_languages)[localePreferences.appLanguage]

  companion object {
    const val POLISH = "pl"
    const val RUSSIAN = "ru"
    const val SPANISH = "es"
    const val UKRAINIAN = "uk"
    const val PORTUGUESE = "pt"
    const val BULGARIAN = "bg"

    fun getScreenLanguage(code: Int): Locale {
      return when (code) {
        0 -> Locale.getDefault()
        1 -> Locale.ENGLISH
        2 -> Locale.GERMAN
        3 -> Locale(SPANISH, "")
        4 -> Locale.FRENCH
        5 -> Locale.ITALIAN
        6 -> Locale(PORTUGUESE, "")
        7 -> Locale(POLISH, "")
        8 -> Locale("cs", "")
        9 -> Locale("ro", "")
        10 -> Locale("tr", "")
        11 -> Locale(UKRAINIAN, "")
        12 -> Locale(RUSSIAN, "")
        13 -> Locale.JAPANESE
        14 -> Locale.CHINESE
        15 -> Locale("hi", "")
        16 -> Locale.KOREAN
        17 -> Locale(BULGARIAN, "")
        else -> Locale.getDefault()
      }
    }
  }
}
