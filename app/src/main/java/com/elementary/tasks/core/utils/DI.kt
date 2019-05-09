package com.elementary.tasks.core.utils

import com.backdoor.engine.Recognizer
import com.elementary.tasks.QrShareProvider
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel
import com.elementary.tasks.experimental.home.HomeViewModel
import com.elementary.tasks.navigation.settings.voice.TimesViewModel
import com.elementary.tasks.notes.create.CreateNoteViewModel
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.places.create.CreatePlaceViewModel
import com.elementary.tasks.reminder.create.StateViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

fun utilModule() = module {
    single { AppDb.getAppDatabase(androidApplication()) }
    single { Prefs(androidApplication()) }
    single { SoundStackHolder(androidApplication(), get()) }
    single { ThemeUtil(androidApplication(), get()) }
    single { BackupTool(get()) }
    single { Dialogues(get()) }
    single { Language(get()) }
    single { Notifier(androidApplication(), get(), get()) }
    single { CalendarUtils(androidApplication(), get(), get()) }
    single { providesRecognizer(get(), get()) }
    single { QrShareProvider(get()) }
    single { GlobalButtonObservable() }
    single { ImagesSingleton() }
}

fun viewModels() = module {
    viewModel { StateViewModel() }
    viewModel { CreatePlaceViewModel() }
    viewModel { CreateNoteViewModel() }
    viewModel { TimesViewModel() }
    viewModel { HomeViewModel() }
    viewModel { ConversationViewModel() }
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

fun components(): List<Module> {
    return listOf(utilModule(), viewModels())
}