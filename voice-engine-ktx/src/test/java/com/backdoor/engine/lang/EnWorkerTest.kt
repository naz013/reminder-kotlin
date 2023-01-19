package com.backdoor.engine.lang

import com.backdoor.engine.Recognizer
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.ActionType
import com.backdoor.engine.misc.ContactsInterface
import com.backdoor.engine.misc.Locale
import com.backdoor.engine.misc.TimeUtil
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class EnWorkerTest {

  private val contactsInterface = mockk<ContactsInterface>()
  private val recognizer = Recognizer.Builder()
    .setLocale(Locale.EN)
    .setTimes(TIMES)
    .setTimeZone(TIME_ZONE_ID)
    .build()

  @Before
  fun setUp() {
    every { contactsInterface.findNumber(any()) }.answers { null }
    every { contactsInterface.findNumber("home") }.answers { "123456" }
    every { contactsInterface.findEmail(any()) }.answers { null }
    every { contactsInterface.findEmail("home") }.answers { "test@mail.com" }
    recognizer.setContactHelper(contactsInterface)
  }

  @Test
  fun testAfterTomorrow() {
    val input = "after tomorrow at 7 pm release application"
    val model = recognizer.recognize(input)

    val expectedDateTime = LocalDateTime.now()
      .plusDays(2)
      .withHour(19)
      .withMinute(0)
      .withSecond(0)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(TimeUtil.getGmtFromDateTime(expectedDateTime), model?.dateTime)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("release application", model?.summary?.lowercase())
  }

  @Test
  fun testTomorrow() {
    val input = "tomorrow at 7 pm release application"
    val model = recognizer.recognize(input)

    val expectedDateTime = LocalDateTime.now()
      .plusDays(1)
      .withHour(19)
      .withMinute(0)
      .withSecond(0)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(TimeUtil.getGmtFromDateTime(expectedDateTime), model?.dateTime)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("release application", model?.summary?.lowercase())
  }

  @Test
  fun testResponseYes() {
    val model = recognizer.recognize("yes of course")

    assertEquals(true, model != null)
    assertEquals(ActionType.ANSWER, model?.type)
    assertEquals(Action.YES, model?.action)

    val model2 = recognizer.recognize("yes")

    assertEquals(true, model2 != null)
    assertEquals(ActionType.ANSWER, model2?.type)
    assertEquals(Action.YES, model2?.action)
  }

  @Test
  fun testResponseNo() {
    val model = recognizer.recognize("no don't save")

    assertEquals(true, model != null)
    assertEquals(ActionType.ANSWER, model?.type)
    assertEquals(Action.NO, model?.action)

    val model2 = recognizer.recognize("no")

    assertEquals(true, model2 != null)
    assertEquals(ActionType.ANSWER, model2?.type)
    assertEquals(Action.NO, model2?.action)
  }

  @Test
  fun testShowBirthdaysForNextWeek() {
    val input = "show birthdays for next week"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.BIRTHDAYS, model?.action)
    assertEquals(DAY * 7, model?.repeatInterval)
  }

  @Test
  fun testShowRemindersForNext3Days() {
    val input = "show reminders for next 3 days"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.REMINDERS, model?.action)
    assertEquals(DAY * 3, model?.repeatInterval)
  }

  @Test
  fun testDisableReminders() {
    val input = "disable all reminders"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.DISABLE, model?.action)
  }

  @Test
  fun testEmptyArchivedReminders() {
    val input = "empty trash"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.TRASH, model?.action)
  }

  @Test
  fun testClearArchivedReminders() {
    val input = "clear trash"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.TRASH, model?.action)
  }

  @Test
  fun testAddGroup() {
    val input = "create group work"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.GROUP, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("work", model?.summary?.lowercase())
  }

  @Test
  fun testAddGroup2() {
    val input = "add group work"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.GROUP, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("work", model?.summary?.lowercase())
  }

  @Test
  fun testAddGroup3() {
    val input = "add new group work"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.GROUP, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("work", model?.summary?.lowercase())
  }

  @Test
  fun testAddNote() {
    val input = "create note release app on next week"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.NOTE, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("release app on next week", model?.summary?.lowercase())
  }

  @Test
  fun testShowBirthdays() {
    val input = "show birthdays"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.BIRTHDAYS, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("", model?.summary?.lowercase())
  }

  @Test
  fun testShowActiveReminders() {
    val input = "show active reminders"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.ACTIVE_REMINDERS, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("", model?.summary?.lowercase())
  }

  @Test
  fun testShowReminders() {
    val input = "show reminders"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.REMINDERS, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("", model?.summary?.lowercase())
  }

  @Test
  fun testShowGroups() {
    val input = "show groups"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.GROUPS, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("", model?.summary?.lowercase())
  }

  @Test
  fun testShowNotes() {
    val input = "show notes"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.NOTES, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("", model?.summary?.lowercase())
  }

  @Test
  fun testShowShoppingLists() {
    val input = "show shopping lists"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.SHOP_LISTS, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("", model?.summary?.lowercase())
  }

  @Test
  fun testOpenApp() {
    val input = "open application"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.APP, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("", model?.summary?.lowercase())
  }

  @Test
  fun testOpenSettings() {
    val input = "open settings"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.SETTINGS, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("", model?.summary?.lowercase())
  }

  @Test
  fun testOpenVolumeSettings() {
    val input = "change volume settings"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.VOLUME, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("", model?.summary?.lowercase())
  }

  @Test
  fun testOpenHelp() {
    val input = "open help"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.HELP, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("", model?.summary?.lowercase())
  }

  @Test
  fun testTimerMinutes() {
    val input = "after 15 minutes open application"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(15 * MINUTE, model?.afterMillis)
    assertEquals("open application", model?.summary?.lowercase())
  }

  @Test
  fun testTimerHours() {
    val input = "after 3 hours check tests"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(3 * HOUR, model?.afterMillis)
    assertEquals("check tests", model?.summary?.lowercase())
  }

  @Test
  fun testTimerDays() {
    val input = "after 1 day release application"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(DAY, model?.afterMillis)
    assertEquals("release application", model?.summary?.lowercase())
  }

  @Test
  fun testTimerHalfHour() {
    val input = "after half an hour call home"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.CALL, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals("123456", model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(HOUR / 2, model?.afterMillis)
    assertEquals(input, model?.summary?.lowercase())
  }

  @Test
  fun testWeekdayMondayFriday() {
    val input = "check tests every monday and friday at 7 pm"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.WEEK, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(listOf(0, 1, 0, 0, 0, 1, 0), model?.weekdays)
    assertEquals("check tests", model?.summary?.lowercase())
  }

  @Test
  fun testByDateEveryDay() {
    val input = "check tests every day at 2:30 p.m"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals("check tests", model?.summary?.lowercase())
  }

  @Test
  fun testByDateEveryDay2() {
    val input = "check tests everyday at 14:30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals("check tests", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderDecember() {
    val input = "release update on december 25 at 17 o'clock"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(12, 25, 17, 0), model?.dateTime)
    assertEquals("release update", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJanuary() {
    val input = "release update on january second at 12 30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(1, 2, 12, 30), model?.dateTime)
    assertEquals("release update", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderFebruary() {
    val input = "release update february fifth at seven o'clock"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(2, 5, 7, 0), model?.dateTime)
    assertEquals("release update", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderMarch() {
    val input = "release update on march eighteens at 13 45"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(3, 18, 13, 45), model?.dateTime)
    assertEquals("release update", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderApril() {
    val input = "on april 29 at 11 release update"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(4, 29, 11, 0), model?.dateTime)
    assertEquals("release update", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderMay() {
    val input = "release update on may 11 at 15:30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(5, 11, 15, 30), model?.dateTime)
    assertEquals("release update", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJuneWithCall() {
    val input = "on june 10 in the morning call home"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.CALL, model?.action)
    assertEquals("123456", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(6, 10, 7, 0), model?.dateTime)
    assertEquals(input, model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJulyWithSms() {
    val input = "on july first at 16:33 send message to home with text run forest run"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MESSAGE, model?.action)
    assertEquals("123456", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(7, 1, 16, 33), model?.dateTime)
    assertEquals("run forest run", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJulyWithSmsYesText() {
    val input = "on july 5 at 16:33 send message to home with text yes, i came"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MESSAGE, model?.action)
    assertEquals("123456", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(7, 5, 16, 33), model?.dateTime)
    assertEquals("yes, i came", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderAugustWithEmail() {
    val input = "on august 25 in the evening send email to home with text run tests"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MAIL, model?.action)
    assertEquals("test@mail.com", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(8, 25, 19, 0), model?.dateTime)
    assertEquals("run tests", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderAugustWithEmail2() {
    val input = "on august 20 in the morning send letter to home with text run tests"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MAIL, model?.action)
    assertEquals("test@mail.com", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(8, 20, 7, 0), model?.dateTime)
    assertEquals("run tests", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderSeptemberWithRepeat() {
    val input = "on september tenth at night release update and repeat every day"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(9, 10, 23, 0), model?.dateTime)
    assertEquals("release update", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderOctoberWithCalendar() {
    val input = "release update on october 8 at noon and add to calendar"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(true, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(10, 8, 12, 0), model?.dateTime)
    assertEquals("release update", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderNovember() {
    val input = "release update on november 11 at eleven o'clock"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(11, 11, 11, 0), model?.dateTime)
    assertEquals("release update", model?.summary?.lowercase())
  }
}
