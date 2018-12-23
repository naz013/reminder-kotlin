package com.elementary.tasks.core.di

import android.app.Application
import com.backdoor.engine.Recognizer
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.notes.preview.ImagesSingleton
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Copyright 2018 Nazar Suhovich
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
@Module
class UtilModule {

    @Provides
    @Singleton
    fun providesButtonObservable(): GlobalButtonObservable {
        return GlobalButtonObservable()
    }

    @Provides
    @Singleton
    fun providesImagesSingleton(): ImagesSingleton {
        return ImagesSingleton()
    }

    @Provides
    @Singleton
    fun providesPrefs(application: Application): Prefs {
        return Prefs(application)
    }

    @Provides
    @Singleton
    fun providesRemotePrefs(application: Application): RemotePrefs {
        return RemotePrefs(application)
    }

    @Provides
    @Singleton
    fun providesUpdatesHelper(application: Application): UpdatesHelper {
        return UpdatesHelper(application)
    }

    @Provides
    @Singleton
    fun providesSoundStack(application: Application, prefs: Prefs): SoundStackHolder {
        return SoundStackHolder(application, prefs)
    }

    @Provides
    @Singleton
    fun providesThemeUtil(application: Application, prefs: Prefs): ThemeUtil {
        return ThemeUtil(application, prefs)
    }

    @Provides
    @Singleton
    fun providesBackupTool(appDb: AppDb): BackupTool {
        return BackupTool(appDb)
    }

    @Provides
    @Singleton
    fun providesIoHelper(application: Application, prefs: Prefs, backupTool: BackupTool): IoHelper {
        return IoHelper(application, prefs, backupTool)
    }

    @Provides
    @Singleton
    fun providesDialogues(themeUtil: ThemeUtil): Dialogues {
        return Dialogues(themeUtil)
    }

    @Provides
    @Singleton
    fun providesLanguage(prefs: Prefs): Language {
        return Language(prefs)
    }

    @Provides
    @Singleton
    fun providesNotifier(application: Application, prefs: Prefs, themeUtil: ThemeUtil): Notifier {
        return Notifier(application, prefs, themeUtil)
    }

    @Provides
    @Singleton
    fun providesRecognizer(prefs: Prefs, language: Language): Recognizer {
        val lang = language.getLanguage(prefs.voiceLocale)
        val morning = prefs.morningTime
        val day = prefs.noonTime
        val evening = prefs.eveningTime
        val night = prefs.nightTime
        val times = arrayOf(morning, day, evening, night)
        return Recognizer.Builder()
                .setLocale(lang)
                .setTimes(times)
                .build()
    }

    @Provides
    @Singleton
    fun providesCalendarUtils(application: Application, prefs: Prefs, appDb: AppDb): CalendarUtils {
        return CalendarUtils(application, prefs, appDb)
    }
}
