package com.github.naz013.wearsync

/**
 * Wire contract shared by the phone-side transport ([com.github.naz013.wearsync], the
 * `extensions:wearsync` module) and the Wear OS companion app (`wear:wear-app`). Both sides depend
 * on this module only - never on each other - so the path/key strings below are the only thing
 * that has to stay in sync between the two independently-built APKs.
 */
object WearSyncProtocol {
  /** [com.google.android.gms.wearable.DataClient] path the phone publishes the reminder list to. */
  const val REMINDERS_DATA_PATH = "/reminders"

  /** Key of the [com.google.android.gms.wearable.DataMap] array carried at [REMINDERS_DATA_PATH]. */
  const val KEY_REMINDER_ITEMS = "reminder_items"

  const val KEY_ITEM_UU_ID = "uu_id"
  const val KEY_ITEM_TITLE = "title"
  const val KEY_ITEM_EVENT_DATE_TIME_MILLIS = "event_date_time_millis"
  const val KEY_ITEM_GROUP_COLOR = "group_color"
  const val KEY_ITEM_CAN_SNOOZE = "can_snooze"

  /** [com.google.android.gms.wearable.MessageClient] paths the watch sends a reminder id to. */
  const val ACTION_COMPLETE_PATH = "/reminder/complete"
  const val ACTION_SNOOZE_PATH = "/reminder/snooze"

  /** Maximum number of upcoming reminders kept glanceable on the watch. */
  const val MAX_SYNCED_REMINDERS = 25
}
