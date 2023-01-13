package com.elementary.tasks.core.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs
import java.util.*

class Language(
  private val prefs: Prefs,
  private val context: Context
) {

  /**
   * Holder locale for tts.
   *
   * @param isBirth flag for birthdays.
   * @return Locale
   */
  fun getLocale(isBirth: Boolean): Locale? {
    var res: Locale? = null
    when ((if (isBirth) {
      prefs.birthdayTtsLocale
    } else {
      prefs.ttsLocale
    })) {
      ENGLISH -> res = Locale.ENGLISH
      FRENCH -> res = Locale.FRENCH
      GERMAN -> res = Locale.GERMAN
      JAPANESE -> res = Locale.JAPANESE
      ITALIAN -> res = Locale.ITALIAN
      KOREAN -> res = Locale.KOREAN
      POLISH -> res = Locale("pl", "")
      RUSSIAN -> res = Locale("ru", "")
      SPANISH -> res = Locale("es", "")
      UKRAINIAN -> res = Locale("uk", "")
      PORTUGUESE -> res = Locale("pt", "")
    }
    return res
  }

  fun onAttach(context: Context): Context {
    return setLocale(context, getScreenLanguage(prefs.appLanguage))
  }

  private fun setLocale(context: Context, locale: Locale): Context {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      updateResources(context, locale)
    } else updateResourcesLegacy(context, locale)
  }

  @TargetApi(Build.VERSION_CODES.N)
  private fun updateResources(context: Context, locale: Locale): Context {
    Locale.setDefault(locale)
    val configuration = context.resources.configuration
    configuration.setLocale(locale)
    configuration.setLayoutDirection(locale)
    return context.createConfigurationContext(configuration)
  }

  @Suppress("DEPRECATION")
  private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
    Locale.setDefault(locale)
    val resources = context.resources
    val configuration = resources.configuration
    configuration.locale = locale
    runCatching {
      configuration.setLayoutDirection(locale)
      resources.updateConfiguration(configuration, resources.displayMetrics)
    }
    return context
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
    context.getString(R.string.polish)
  )

  fun getTextLanguage(code: Int) = when (code) {
    0 -> ENGLISH
    1 -> UKRAINIAN
    2 -> SPANISH
    3 -> PORTUGUESE
    4 -> POLISH
    else -> ENGLISH
  }

  fun getLanguage(code: Int) = when (code) {
    0 -> EN
    1 -> UK
    2 -> ES
    3 -> PT
    4 -> PL
    else -> EN
  }

  fun getVoiceLocale(code: Int): Locale = when (code) {
    0 -> Locale.ENGLISH
    1 -> Locale("uk", "")
    2 -> Locale("es", "")
    3 -> Locale("pt", "")
    4 -> Locale("pl", "")
    else -> Locale.ENGLISH
  }

  fun getVoiceLanguage(code: Int) = when (code) {
    0 -> com.backdoor.engine.misc.Locale.EN
    1 -> com.backdoor.engine.misc.Locale.UK
    2 -> com.backdoor.engine.misc.Locale.ES
    3 -> com.backdoor.engine.misc.Locale.PT
    4 -> com.backdoor.engine.misc.Locale.PL
    else -> com.backdoor.engine.misc.Locale.EN
  }

  fun getLocaleByPosition(position: Int) = when (position) {
    0 -> ENGLISH
    1 -> FRENCH
    2 -> GERMAN
    3 -> ITALIAN
    4 -> JAPANESE
    5 -> KOREAN
    6 -> POLISH
    7 -> RUSSIAN
    8 -> SPANISH
    9 -> PORTUGUESE
    10 -> UKRAINIAN
    else -> ENGLISH
  }

  fun getLocalePosition(locale: String?) = when {
    locale == null -> 0
    locale.matches(ENGLISH.toRegex()) -> 0
    locale.matches(FRENCH.toRegex()) -> 1
    locale.matches(GERMAN.toRegex()) -> 2
    locale.matches(ITALIAN.toRegex()) -> 3
    locale.matches(JAPANESE.toRegex()) -> 4
    locale.matches(KOREAN.toRegex()) -> 5
    locale.matches(POLISH.toRegex()) -> 6
    locale.matches(RUSSIAN.toRegex()) -> 7
    locale.matches(SPANISH.toRegex()) -> 8
    locale.matches(PORTUGUESE.toRegex()) -> 9
    locale.matches(UKRAINIAN.toRegex()) -> 10
    else -> 0
  }

  fun getScreenLocaleName(context: Context): String =
    context.resources.getStringArray(R.array.app_languages)[prefs.appLanguage]

  fun getLocaleNames(context: Context?) =
    context?.let {
      listOf(
        context.getString(R.string.english),
        context.getString(R.string.french),
        context.getString(R.string.german),
        context.getString(R.string.italian),
        context.getString(R.string.japanese),
        context.getString(R.string.korean),
        context.getString(R.string.polish),
        context.getString(R.string.russian),
        context.getString(R.string.spanish),
        context.getString(R.string.portuguese),
        context.getString(R.string.ukrainian)
      )
    } ?: listOf()

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

    private const val EN = "en-US"
    private const val UK = "uk-UA"
    private const val ES = "es-ES"
    private const val PT = "pt-PT"
    private const val PL = "pl"

    fun getScreenLanguage(code: Int): Locale {
      return when (code) {
        0 -> Locale.getDefault()
        1 -> Locale.ENGLISH
        2 -> Locale.GERMAN
        3 -> Locale("es", "")
        4 -> Locale.FRENCH
        5 -> Locale.ITALIAN
        6 -> Locale("pt", "")
        7 -> Locale("pl", "")
        8 -> Locale("cs", "")
        9 -> Locale("ro", "")
        10 -> Locale("tr", "")
        11 -> Locale("uk", "")
        12 -> Locale("ru", "")
        13 -> Locale.JAPANESE
        14 -> Locale.CHINESE
        15 -> Locale("hi", "")
        16 -> Locale.KOREAN
        else -> Locale.getDefault()
      }
    }
  }
}
