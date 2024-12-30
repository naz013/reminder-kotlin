package com.elementary.tasks.core.deeplink

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.github.naz013.ui.common.activity.DeepLinkData

class DeepLinkDataParser {

  private val deepLinkDataMap = mapOf(
    Pair(IntentKey.REMINDER_DATETIME_TYPE, ReminderDatetimeTypeDeepLinkData::class.java),
    Pair(IntentKey.REMINDER_TODO_TYPE, ReminderTodoTypeDeepLinkData::class.java),
    Pair(IntentKey.BIRTHDAY_DATE, BirthdayDateDeepLinkData::class.java),
    Pair(IntentKey.GOOGLE_TASK_DATE_TIME, GoogleTaskDateTimeDeepLinkData::class.java)
  )

  fun readDeepLinkData(
    intent: Intent
  ): DeepLinkData? {
    val key = findKey(intent) ?: return null
    val clazz = deepLinkDataMap[key] ?: return null
    return readParcelable(intent, key, clazz)
  }

  fun readDeepLinkData(
    bundle: Bundle
  ): DeepLinkData? {
    val key = findKey(bundle) ?: return null
    val clazz = deepLinkDataMap[key] ?: return null
    return readParcelable(bundle, key, clazz)
  }

  private fun findKey(intent: Intent): String? {
    return deepLinkDataMap.keys.firstOrNull { intent.hasExtra(it) }
  }

  private fun findKey(bundle: Bundle): String? {
    return deepLinkDataMap.keys.firstOrNull { bundle.containsKey(it) }
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

  private fun <T : DeepLinkData> readParcelable(
    bundle: Bundle,
    key: String,
    clazz: Class<T>
  ): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      bundle.getParcelable(key, clazz)
    } else {
      bundle.getParcelable(key) as? T
    }
  }
}
