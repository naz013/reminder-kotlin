package com.elementary.tasks.core.utils

import android.app.Application
import com.backdoor.engine.Recognizer
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.notes.preview.ImagesSingleton
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

fun utilModule(application: Application) = module {
    single { application.applicationContext }
    single { AppDb.getAppDatabase(application) }
    single { Prefs(application) }
    single { SoundStackHolder(application, get()) }
    single { ThemeUtil(application, get()) }
    single { BackupTool(get()) }
    single { Dialogues(get()) }
    single { Language(get()) }
    single { Notifier(application, get(), get()) }
    single { CalendarUtils(application, get(), get()) }
    single { providesRecognizer(get(), get()) }
    single { GlobalButtonObservable() }
    single { ImagesSingleton() }
}

fun providesRecognizer(prefs: Prefs, language: Language): Recognizer {
    val lang = language.getVoiceLanguage(prefs.voiceLocale)
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

fun components(application: Application): List<Module> {
    return listOf(utilModule(application))
}