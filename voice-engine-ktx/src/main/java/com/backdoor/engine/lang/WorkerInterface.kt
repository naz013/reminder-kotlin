package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.LongInternal
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

internal interface WorkerInterface {
  fun splitWords(input: String?): String?
  fun replaceNumbers(input: String?): String?
  fun hasCalendar(input: String): Boolean
  fun clearCalendar(input: String): String?
  fun getWeekDays(input: String): List<Int>
  fun clearWeekDays(input: String): String
  fun getDaysRepeat(input: String): Long
  fun clearDaysRepeat(input: String): String?
  fun clearRepeat(input: String): String?
  fun hasTomorrow(input: String): Boolean
  fun clearTomorrow(input: String): String?
  fun getMessage(input: String): String
  fun clearMessage(input: String): String?
  fun clearMessageType(input: String): String?
  fun clearAmpm(input: String): String?
  fun getTime(input: String, ampm: Ampm?, times: List<String>): LocalTime?
  fun clearTime(input: String?): String
  fun getDateAndClear(input: String, result: (LocalDate?) -> Unit): String?
  fun clearCall(input: String): String?
  fun cleanTimer(input: String): String?
  fun hasSender(input: String): Boolean
  fun clearSender(input: String): String?
  fun findSenderAndClear(input: String, action: Action, result: (String) -> Unit): String
  fun hasNote(input: String): Boolean
  fun clearNote(input: String): String
  fun hasAction(input: String): Boolean
  fun getAction(input: String): Action?
  fun hasEvent(input: String): Boolean
  fun getEvent(input: String): Action?
  fun getMultiplier(input: String, res: LongInternal): String
  fun hasDisableReminders(input: String): Boolean
  fun hasEmptyTrash(input: String): Boolean
  fun hasGroup(input: String): Boolean
  fun clearGroup(input: String): String
  fun hasToday(input: String): Boolean
  fun clearToday(input: String): String
  fun hasAfterTomorrow(input: String): Boolean
  fun clearAfterTomorrow(input: String): String
  fun hasAnswer(input: String): Boolean
  fun getAnswer(input: String): Action?
  fun hasShowAction(input: String): Boolean
  fun getShowAction(input: String): Action?
  fun clearShowAction(input: String): String
  fun hasNextModifier(input: String): Boolean
  fun hasCall(input: String): Boolean
  fun hasRepeat(input: String): Boolean
  fun hasEveryDay(input: String): Boolean
  fun getMessageType(input: String): Action?
  fun getAmpm(input: String): Ampm?
  fun hasTimer(input: String): Boolean
}
