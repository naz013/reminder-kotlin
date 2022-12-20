package com.backdoor.engine.lang

import com.backdoor.engine.misc.Locale
import org.threeten.bp.ZoneId

internal object WorkerFactory {

  fun getWorker(locale: String, zoneId: ZoneId) = when (locale) {
    Locale.EN -> EnWorker(zoneId)
    Locale.UK -> UkWorker(zoneId)
    Locale.RU -> RuWorker(zoneId)
    Locale.DE -> DeWorker(zoneId)
    Locale.ES -> EsWorker(zoneId)
    Locale.PT -> PtWorker(zoneId)
    Locale.PL -> PlWorker(zoneId)
    else -> EnWorker(zoneId)
  }.also {
    println("getWorker: $locale, $it")
  }
}
