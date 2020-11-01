package com.elementary.tasks.core.utils

import com.backdoor.engine.Recognizer
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.notes.preview.ImagesSingleton
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

fun utilModule() = module {
  single { AppDb.getAppDatabase(androidApplication()) }
  single { Prefs.getInstance(androidApplication()) }
  single { SoundStackHolder(androidApplication(), get()) }
  single { ThemeUtil(androidApplication(), get()) }
  single { BackupTool(get()) }
  single { Dialogues() }
  single { Language(get()) }
  single { CalendarUtils(androidApplication(), get(), get()) }
  single { providesRecognizer(get(), get()) }
  single { CacheUtil(get()) }
  single { GlobalButtonObservable() }
  single { ImagesSingleton() }
}

fun providesRecognizer(prefs: Prefs, language: Language): Recognizer {
  val morning = prefs.morningTime
  val day = prefs.noonTime
  val evening = prefs.eveningTime
  val night = prefs.nightTime
  return Recognizer.Builder()
    .setLocale(language.getVoiceLanguage(prefs.voiceLocale))
    .setTimes(listOf(morning, day, evening, night))
    .build()
}