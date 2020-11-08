package com.backdoor.engine.lang

import com.backdoor.engine.misc.Locale

internal object WorkerFactory {

  fun getWorker(locale: String) = when (locale) {
    Locale.EN -> EnWorker()
    Locale.UK -> UkWorker()
    Locale.RU -> RuWorker()
    Locale.DE -> DeWorker()
    Locale.ES -> EsWorker()
    Locale.PT -> PtWorker()
    Locale.PL -> PlWorker()
    else -> EnWorker()
  }.also {
    println("getWorker: $locale, $it")
  }
}