package com.backdoor.engine.lang

import com.backdoor.engine.misc.Locale

internal object WorkerFactory {
  fun getWorker(locale: String): WorkerInterface {
    println("getWorker: $locale")
    return when (locale) {
      Locale.EN -> EnWorker()
      Locale.UK -> UkWorker()
      Locale.RU -> RuWorker()
      Locale.DE -> DeWorker()
      Locale.ES -> EsWorker()
      Locale.PT -> PtWorker()
      else -> EnWorker()
    }
  }
}