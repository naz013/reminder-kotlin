package com.backdoor.engine.lang

import com.backdoor.engine.misc.ContactsInterface
import com.backdoor.engine.misc.Locale
import org.threeten.bp.ZoneId

internal object WorkerFactory {

  fun getWorker(
    locale: String,
    zoneId: ZoneId,
    contactsInterface: ContactsInterface?
  ): WorkerInterface {
    return when (locale) {
      Locale.EN -> EnWorker(zoneId, contactsInterface)
      Locale.UK -> UkWorker(zoneId, contactsInterface)
      Locale.ES -> EsWorker(zoneId, contactsInterface)
      Locale.PT -> PtWorker(zoneId, contactsInterface)
      Locale.PL -> PlWorker(zoneId, contactsInterface)
      Locale.IT -> ItWorker(zoneId, contactsInterface)
      else -> EnWorker(zoneId, contactsInterface)
    }.also {
      println("getWorker: $locale, $it")
    }
  }
}
