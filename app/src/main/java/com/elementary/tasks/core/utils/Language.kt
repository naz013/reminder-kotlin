package com.elementary.tasks.core.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.elementary.tasks.R
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

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
@Singleton
class Language @Inject constructor(private val prefs: Prefs){

    /**
     * Holder locale for tts.
     *
     * @param isBirth flag for birthdays.
     * @return Locale
     */
    fun getLocale(isBirth: Boolean): Locale? {
        var res: Locale? = null
        val locale: String = (if (isBirth) {
            prefs.birthdayTtsLocale
        } else {
            prefs.ttsLocale
        })
        when (locale) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    @Suppress("DEPRECATION")
    fun getLocalized(context: Context, id: Int): String {
        return if (Module.isJellyMR1) {
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(Locale(getTextLanguage(prefs.voiceLocale)))
            context.createConfigurationContext(configuration).resources.getString(id)
        } else {
            val standardResources = context.resources
            val assets = standardResources.assets
            val metrics = standardResources.displayMetrics
            val config = Configuration(standardResources.configuration)
            config.locale = Locale(getTextLanguage(prefs.voiceLocale))
            val defaultResources = Resources(assets, metrics, config)
            defaultResources.getString(id)
        }
    }

    fun getLanguages(context: Context): List<String> {
        val locales = ArrayList<String>()
        locales.add(context.getString(R.string.english) + " (" + EN + ")")
        locales.add(context.getString(R.string.russian) + " (" + RU + ")")
        locales.add(context.getString(R.string.ukrainian) + " (" + UK + ")")
        locales.add(context.getString(R.string.german) + " (" + DE + ") (BETA)")
        locales.add(context.getString(R.string.spanish) + " (" + ES + ") (BETA)")
        locales.add(context.getString(R.string.portuguese) + " (" + PT + ") (BETA)")
        return locales
    }

    private fun getScreenLanguage(code: Int): Locale {
        when (code) {
            0 -> return Locale.getDefault()
            1 -> return Locale.ENGLISH
            2 -> return Locale.GERMAN
            3 -> return Locale("es", "")
            4 -> return Locale.FRENCH
            5 -> return Locale.ITALIAN
            6 -> return Locale("pt", "")
            7 -> return Locale("pl", "")
            8 -> return Locale("cs", "")
            9 -> return Locale("ro", "")
            10 -> return Locale("tr", "")
            11 -> return Locale("uk", "")
            12 -> return Locale("ru", "")
            13 -> return Locale.JAPANESE
            14 -> return Locale.CHINESE
            15 -> return Locale("hi", "")
            else -> return Locale.getDefault()
        }
    }

    fun getTextLanguage(code: Int): String {
        return when (code) {
            0 -> ENGLISH
            1 -> RUSSIAN
            2 -> UKRAINIAN
            3 -> GERMAN
            4 -> SPANISH
            5 -> PORTUGUESE
            else -> ENGLISH
        }
    }

    fun getLanguage(code: Int): String {
        return when (code) {
            0 -> EN
            1 -> RU
            2 -> UK
            3 -> DE
            4 -> ES
            5 -> PT
            else -> EN
        }
    }

    fun getVoiceLanguage(code: Int): String {
        return when (code) {
            0 -> com.backdoor.engine.misc.Locale.EN
            1 -> com.backdoor.engine.misc.Locale.RU
            2 -> com.backdoor.engine.misc.Locale.UK
            3 -> com.backdoor.engine.misc.Locale.DE
            4 -> com.backdoor.engine.misc.Locale.ES
            5 -> com.backdoor.engine.misc.Locale.PT
            else -> com.backdoor.engine.misc.Locale.EN
        }
    }

    fun getLocaleByPosition(position: Int): String {
        var locale = Language.ENGLISH
        if (position == 0) locale = Language.ENGLISH
        else if (position == 1) locale = Language.FRENCH
        else if (position == 2) locale = Language.GERMAN
        else if (position == 3) locale = Language.ITALIAN
        else if (position == 4) locale = Language.JAPANESE
        else if (position == 5) locale = Language.KOREAN
        else if (position == 6) locale = Language.POLISH
        else if (position == 7) locale = Language.RUSSIAN
        else if (position == 8) locale = Language.SPANISH
        else if (position == 9) locale = Language.PORTUGUESE
        else if (position == 10 && Module.isJellyMR2) locale = Language.UKRAINIAN
        return locale
    }

    fun getLocalePosition(locale: String?): Int {
        if (locale == null) {
            return 0
        }
        var mItemSelect = 0
        if (locale.matches(Language.ENGLISH.toRegex())) {
            mItemSelect = 0
        } else if (locale.matches(Language.FRENCH.toRegex())) {
            mItemSelect = 1
        } else if (locale.matches(Language.GERMAN.toRegex())) {
            mItemSelect = 2
        } else if (locale.matches(Language.ITALIAN.toRegex())) {
            mItemSelect = 3
        } else if (locale.matches(Language.JAPANESE.toRegex())) {
            mItemSelect = 4
        } else if (locale.matches(Language.KOREAN.toRegex())) {
            mItemSelect = 5
        } else if (locale.matches(Language.POLISH.toRegex())) {
            mItemSelect = 6
        } else if (locale.matches(Language.RUSSIAN.toRegex())) {
            mItemSelect = 7
        } else if (locale.matches(Language.SPANISH.toRegex())) {
            mItemSelect = 8
        } else if (locale.matches(Language.PORTUGUESE.toRegex())) {
            mItemSelect = 9
        } else if (locale.matches(Language.UKRAINIAN.toRegex()) && Module.isJellyMR2) {
            mItemSelect = 10
        }
        return mItemSelect
    }

    fun getScreenLocaleName(context: Context): String {
        return context.resources.getStringArray(R.array.app_languages)[prefs.appLanguage]
    }

    fun getScreenLocaleNames(context: Context): List<String> {
        val names = ArrayList<String>()
        Collections.addAll(names, *context.resources.getStringArray(R.array.app_languages))
        return names
    }

    fun getLocaleNames(context: Context): List<String> {
        val names = ArrayList<String>()
        names.add(context.getString(R.string.english))
        names.add(context.getString(R.string.french))
        names.add(context.getString(R.string.german))
        names.add(context.getString(R.string.italian))
        names.add(context.getString(R.string.japanese))
        names.add(context.getString(R.string.korean))
        names.add(context.getString(R.string.polish))
        names.add(context.getString(R.string.russian))
        names.add(context.getString(R.string.spanish))
        names.add(context.getString(R.string.portuguese))
        if (Module.isJellyMR2) {
            names.add(context.getString(R.string.ukrainian))
        }
        return names
    }

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
        private const val RU = "ru-RU"
        private const val UK = "uk-UA"
        private const val DE = "de-DE"
        private const val ES = "es-ES"
        private const val PT = "pt-PT"
    }
}
