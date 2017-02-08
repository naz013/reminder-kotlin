package com.elementary.tasks.core.utils;

import android.content.Context;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Prefs;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Language {
    public static final String ENGLISH = "en";
    public static final String FRENCH = "fr";
    public static final String GERMAN = "de";
    public static final String ITALIAN = "it";
    public static final String JAPANESE = "ja";
    public static final String KOREAN = "ko";
    public static final String POLISH = "pl";
    public static final String RUSSIAN = "ru";
    public static final String SPANISH = "es";

    public static final String EN = "en-US";
    public static final String RU = "ru-RU";
    public static final String UK = "uk-UA";

    public static ArrayList<String> getLanguages(Context context) {
        ArrayList<String> locales = new ArrayList<>();
        locales.add(context.getString(R.string.english) + " (" + EN + ")");
        locales.add(context.getString(R.string.russian) + " (" + RU + ")");
        locales.add(context.getString(R.string.ukrainian) + " (" + UK + ")");
        return locales;
    }

    public static String getLanguage(int code) {
        switch (code) {
            case 0:
                return EN;
            case 1:
                return RU;
            case 2:
                return UK;
            default:
                return EN;
        }
    }

    /**
     * Get locale for tts.
     * @param context application context.
     * @param birth flag for birthdays.
     * @return Locale
     */
    public Locale getLocale(Context context, boolean birth){
        Locale res = null;
        String locale;
        if (birth) locale = Prefs.getInstance(context).getBirthdayTtsLocale();
        else locale = Prefs.getInstance(context).getTtsLocale();
        switch (locale){
            case ENGLISH:
                res = Locale.ENGLISH;
                break;
            case FRENCH:
                res = Locale.FRENCH;
                break;
            case GERMAN:
                res = Locale.GERMAN;
                break;
            case JAPANESE:
                res = Locale.JAPANESE;
                break;
            case ITALIAN:
                res = Locale.ITALIAN;
                break;
            case KOREAN:
                res = Locale.KOREAN;
                break;
            case POLISH:
                res = new Locale("pl", "");
                break;
            case RUSSIAN:
                res = new Locale("ru", "");
                break;
            case SPANISH:
                res = new Locale("es", "");
                break;
        }
        return res;
    }
}
