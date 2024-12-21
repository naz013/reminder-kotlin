package com.elementary.tasks.core.utils

import android.content.Context
import android.content.res.Configuration
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.common.android.TextProvider
import java.util.Locale

class Language(
  private val prefs: Prefs,
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
    return setLocale(context, getScreenLanguage(prefs.appLanguage)).also {
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

  fun getConversationLocalizedText(context: Context, id: Int): String {
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(Locale(getTextLanguage(prefs.voiceLocale)))
    return context.createConfigurationContext(configuration).resources.getString(id)
  }

  fun getConversationLocalizedContext(): Context {
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(Locale(getTextLanguage(prefs.voiceLocale)))
    return context.createConfigurationContext(configuration)
  }

  fun getLanguages(context: Context) = listOf(
    context.getString(R.string.english),
    context.getString(R.string.ukrainian),
    context.getString(R.string.spanish),
    context.getString(R.string.portuguese),
    context.getString(R.string.polish),
    context.getString(R.string.italian)
  )

  fun getTextLanguage(code: Int) = when (code) {
    0 -> ENGLISH
    1 -> UKRAINIAN
    2 -> SPANISH
    3 -> PORTUGUESE
    4 -> POLISH
    5 -> ITALIAN
    else -> ENGLISH
  }

  fun getLanguage(code: Int) = when (code) {
    0 -> EN
    1 -> UK
    2 -> ES
    3 -> PT
    4 -> PL
    5 -> IT
    else -> EN
  }

  fun getVoiceLocale(code: Int): Locale = when (code) {
    0 -> Locale.ENGLISH
    1 -> Locale("uk", "")
    2 -> Locale("es", "")
    3 -> Locale("pt", "")
    4 -> Locale("pl", "")
    5 -> Locale.ITALIAN
    else -> Locale.ENGLISH
  }

  fun getVoiceLanguage(code: Int) = when (code) {
    0 -> com.backdoor.engine.misc.Locale.EN
    1 -> com.backdoor.engine.misc.Locale.UK
    2 -> com.backdoor.engine.misc.Locale.ES
    3 -> com.backdoor.engine.misc.Locale.PT
    4 -> com.backdoor.engine.misc.Locale.PL
    5 -> com.backdoor.engine.misc.Locale.IT
    else -> com.backdoor.engine.misc.Locale.EN
  }

  fun getScreenLocaleName(context: Context): String =
    context.resources.getStringArray(R.array.app_languages)[prefs.appLanguage]

  companion object {
    const val ENGLISH = "en"
    const val FRENCH = "fr"
    const val GERMAN = "de"
    const val ITALIAN = "it"
    const val JAPANESE = "ja"
    const val KOREAN = "ko"
    const val POLISH = "pl"
    const val RUSSIAN = "ru"
    const val SPANISH = "es"
    const val UKRAINIAN = "uk"
    const val PORTUGUESE = "pt"
    const val BULGARIAN = "bg"

    private const val EN = "en-US"
    private const val UK = "uk-UA"
    private const val ES = "es-ES"
    private const val PT = "pt-PT"
    private const val PL = "pl"
    private const val IT = "it"

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
