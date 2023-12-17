package com.elementary.tasks.core.deeplink

import android.content.Intent
import android.os.Build

class DeepLinkDataParser {

  private val deepLinkDataMap = mapOf(
    Pair(IntentKey.REMINDER_DATETIME_TYPE, ReminderDatetimeTypeDeepLinkData::class.java),
    Pair(IntentKey.BIRTHDAY_DATE, BirthdayDateDeepLinkData::class.java),
    Pair(IntentKey.GOOGLE_TASK_DATE_TIME, GoogleTaskDateTimeDeepLinkData::class.java)
  )

  fun readDeepLinkData(
    intent: Intent
  ): DeepLinkData? {
    val key = findKey(intent) ?: return null
    val clazz = deepLinkDataMap[key] ?: return null
    return readParcelable(intent, key.value, clazz)
  }

  private fun findKey(intent: Intent): IntentKey? {
    return deepLinkDataMap.keys.firstOrNull { intent.hasExtra(it.value) }
  }

  private fun <T : DeepLinkData> readParcelable(
    intent: Intent,
    key: String,
    clazz: Class<T>
  ): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      intent.getParcelableExtra(key, clazz)
    } else {
      intent.getParcelableExtra(key) as? T
    }
  }
}
