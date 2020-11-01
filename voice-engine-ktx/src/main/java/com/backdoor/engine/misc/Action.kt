package com.backdoor.engine.misc

enum class Action {
  /**
   * Reminder actions
   */
  WEEK, WEEK_CALL, WEEK_SMS, CALL, MESSAGE, MAIL, DATE,

  /**
   * App actions
   */
  SETTINGS, APP, VOLUME, HELP, REMINDER, BIRTHDAY, REPORT, DISABLE, SHOW, TRASH, NO_EVENT,

  /**
   * Answer actions
   */
  YES, NO,

  /**
   * Show actions
   */
  GROUPS, NOTES, ACTIVE_REMINDERS, REMINDERS, BIRTHDAYS, SHOP_LISTS, EVENTS,

  NONE
}