package com.elementary.tasks.core.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.DisplayMetrics;

import com.elementary.tasks.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    public static final String UKRAINIAN = "uk";

    public static final String EN = "en-US";
    public static final String RU = "ru-RU";
    public static final String UK = "uk-UA";

    public static Context onAttach(Context context) {
        return setLocale(context, getScreenLanguage(Prefs.getInstance(context).getAppLanguage()));
    }

    public static Context setLocale(Context context, Locale locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, locale);
        }
        return updateResourcesLegacy(context, locale);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, Locale locale) {
        Locale.setDefault(locale);
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, Locale locale) {
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }

    @NonNull
    public static String getLocalized(Context context, int id) {
        if (Module.isJellyMR1()) {
            Configuration configuration = new Configuration(context.getResources().getConfiguration());
            configuration.setLocale(new Locale(Language.getTextLanguage(Prefs.getInstance(context).getVoiceLocale())));
            return context.createConfigurationContext(configuration).getResources().getString(id);
        } else {
            Resources standardResources = context.getResources();
            AssetManager assets = standardResources.getAssets();
            DisplayMetrics metrics = standardResources.getDisplayMetrics();
            Configuration config = new Configuration(standardResources.getConfiguration());
            config.locale = new Locale(Language.getTextLanguage(Prefs.getInstance(context).getVoiceLocale()));
            Resources defaultResources = new Resources(assets, metrics, config);
            return defaultResources.getString(id);
        }
    }

    @NonNull
    public static List<String> getLanguages(Context context) {
        List<String> locales = new ArrayList<>();
        locales.add(context.getString(R.string.english) + " (" + EN + ")");
        locales.add(context.getString(R.string.russian) + " (" + RU + ")");
        locales.add(context.getString(R.string.ukrainian) + " (" + UK + ")");
        return locales;
    }

    @NonNull
    public static Locale getScreenLanguage(int code) {
        switch (code) {
            case 0:
                return Locale.getDefault();
            case 1:
                return Locale.ENGLISH;
            case 2:
                return Locale.GERMAN;
            case 3:
                return new Locale("es", "");
            case 4:
                return Locale.FRENCH;
            case 5:
                return Locale.ITALIAN;
            case 6:
                return new Locale("pt", "");
            case 7:
                return new Locale("pl", "");
            case 8:
                return new Locale("cs", "");
            case 9:
                return new Locale("ro", "");
            case 10:
                return new Locale("tr", "");
            case 11:
                return new Locale("uk", "");
            case 12:
                return new Locale("ru", "");
            case 13:
                return Locale.JAPANESE;
            case 14:
                return Locale.CHINESE;
            case 15:
                return new Locale("hi", "");
            default:
                return Locale.getDefault();
        }
    }

    @NonNull
    public static String getTextLanguage(int code) {
        switch (code) {
            case 0:
                return ENGLISH;
            case 1:
                return RUSSIAN;
            case 2:
                return UKRAINIAN;
            default:
                return ENGLISH;
        }
    }

    @NonNull
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

    @NonNull
    public static String getLocaleByPosition(int position) {
        String locale = Language.ENGLISH;
        if (position == 0) locale = Language.ENGLISH;
        if (position == 1) locale = Language.FRENCH;
        if (position == 2) locale = Language.GERMAN;
        if (position == 3) locale = Language.ITALIAN;
        if (position == 4) locale = Language.JAPANESE;
        if (position == 5) locale = Language.KOREAN;
        if (position == 6) locale = Language.POLISH;
        if (position == 7) locale = Language.RUSSIAN;
        if (position == 8) locale = Language.SPANISH;
        if (position == 9 && Module.isJellyMR2()) locale = Language.UKRAINIAN;
        return locale;
    }

    public static int getLocalePosition(@Nullable String locale) {
        if (locale == null) {
            return 0;
        }
        int mItemSelect = 0;
        if (locale.matches(Language.ENGLISH)) {
            mItemSelect = 0;
        } else if (locale.matches(Language.FRENCH)) {
            mItemSelect = 1;
        } else if (locale.matches(Language.GERMAN)) {
            mItemSelect = 2;
        } else if (locale.matches(Language.ITALIAN)) {
            mItemSelect = 3;
        } else if (locale.matches(Language.JAPANESE)) {
            mItemSelect = 4;
        } else if (locale.matches(Language.KOREAN)) {
            mItemSelect = 5;
        } else if (locale.matches(Language.POLISH)) {
            mItemSelect = 6;
        } else if (locale.matches(Language.RUSSIAN)) {
            mItemSelect = 7;
        } else if (locale.matches(Language.SPANISH)) {
            mItemSelect = 8;
        } else if (locale.matches(Language.UKRAINIAN) && Module.isJellyMR2()) {
            mItemSelect = 9;
        }
        return mItemSelect;
    }

    @NonNull
    public static String getScreenLocaleName(Context context) {
        return context.getResources().getStringArray(R.array.app_languages)[Prefs.getInstance(context).getAppLanguage()];
    }

    @NonNull
    public static List<String> getScreenLocaleNames(Context context) {
        ArrayList<String> names = new ArrayList<>();
        Collections.addAll(names, context.getResources().getStringArray(R.array.app_languages));
        return names;
    }

    @NonNull
    public static List<String> getLocaleNames(Context mContext) {
        ArrayList<String> names = new ArrayList<>();
        names.add(mContext.getString(R.string.english));
        names.add(mContext.getString(R.string.french));
        names.add(mContext.getString(R.string.german));
        names.add(mContext.getString(R.string.italian));
        names.add(mContext.getString(R.string.japanese));
        names.add(mContext.getString(R.string.korean));
        names.add(mContext.getString(R.string.polish));
        names.add(mContext.getString(R.string.russian));
        names.add(mContext.getString(R.string.spanish));
        if (Module.isJellyMR2()) {
            names.add(mContext.getString(R.string.ukrainian));
        }
        return names;
    }

    /**
     * Holder locale for tts.
     *
     * @param context application context.
     * @param isBirth flag for birthdays.
     * @return Locale
     */
    @Nullable
    public Locale getLocale(Context context, boolean isBirth) {
        Locale res = null;
        String locale;
        if (isBirth) {
            locale = Prefs.getInstance(context).getBirthdayTtsLocale();
        } else {
            locale = Prefs.getInstance(context).getTtsLocale();
        }
        if (locale == null) {
            return Locale.ENGLISH;
        }
        switch (locale) {
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
            case UKRAINIAN:
                res = new Locale("uk", "");
                break;
        }
        return res;
    }
}
