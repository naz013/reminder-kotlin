package com.github.naz013.ui.common.locale

import android.content.Context
import androidx.core.os.LocaleListCompat
import java.util.Locale

class Language(
  private val context: Context,
) {

  fun getCurrentLocale(): String {
    val defLocale = context.resources.configuration.locale
    val locale = runCatching { context.resources.configuration.locales.get(0) }.getOrNull()
      ?: defLocale
    return locale.language
  }

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

    /** Index 0 is "system default" - an empty list tells [androidx.appcompat.app.AppCompatDelegate]
     *  to follow the device locale rather than pinning to whatever [Locale.getDefault] was at
     *  selection time. */
    fun getLocaleList(code: Int): LocaleListCompat =
      if (code == 0) {
        LocaleListCompat.getEmptyLocaleList()
      } else {
        LocaleListCompat.create(getScreenLanguage(code))
      }
  }
}
