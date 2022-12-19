package com.backdoor.engine.lang

import com.backdoor.engine.Recognizer
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.ActionType
import com.backdoor.engine.misc.ContactOutput
import com.backdoor.engine.misc.ContactsInterface
import com.backdoor.engine.misc.Locale
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UkWorkerTest {

  private val contactsInterface = mockk<ContactsInterface>()
  private val recognizer = Recognizer.Builder()
    .setLocale(Locale.UK)
    .setTimes(TIMES)
    .setTimeZone("GMT")
    .build()

  @Before
  fun setUp() {
    every { contactsInterface.findNumber("до дому") }.answers { ContactOutput("до", "123456") }
    every { contactsInterface.findEmail("до дому") }.answers { ContactOutput("до", "test@mail.com") }
    recognizer.setContactHelper(contactsInterface)
  }

  @Test
  fun testResponseYes() {
    val model = recognizer.recognize("так звичайно")

    assertEquals(true, model != null)
    assertEquals(ActionType.ANSWER, model?.type)
    assertEquals(Action.YES, model?.action)

    val model2 = recognizer.recognize("так")

    assertEquals(true, model2 != null)
    assertEquals(ActionType.ANSWER, model2?.type)
    assertEquals(Action.YES, model2?.action)
  }

  @Test
  fun testResponseNo() {
    val model = recognizer.recognize("ні не зберігай")

    assertEquals(true, model != null)
    assertEquals(ActionType.ANSWER, model?.type)
    assertEquals(Action.NO, model?.action)

    val model2 = recognizer.recognize("ні")

    assertEquals(true, model2 != null)
    assertEquals(ActionType.ANSWER, model2?.type)
    assertEquals(Action.NO, model2?.action)
  }

  @Test
  fun testShowBirthdaysForNextWeek() {
    val input = "покажи дні народження на наступний тиждень"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.BIRTHDAYS, model?.action)
    assertEquals(DAY * 7, model?.repeatInterval)
  }

  @Test
  fun testShowRemindersForNext3Days() {
    val input = "покажи нагадування на наступні 3 дні"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.REMINDERS, model?.action)
    assertEquals(DAY * 3, model?.repeatInterval)
  }

  @Test
  fun testDisableReminders() {
    val input = "вимкни всі нагадування"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.DISABLE, model?.action)
  }

  @Test
  fun testClearArchivedReminders() {
    val input = "очисти кошик"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.TRASH, model?.action)
  }

  @Test
  fun testAddGroup() {
    val input = "додай групу робота"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.GROUP, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("робота", model?.summary?.lowercase())
  }

  @Test
  fun testAddNote() {
    val input = "нова нотатка випустити реліз в наступний вівторок"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.NOTE, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("випустити реліз в наступний вівторок", model?.summary?.lowercase())
  }

  @Test
  fun testShowBirthdays() {
    val input = "покажи дні народження"
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
    val input = "покажи активні нагадування"
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
    val input = "показати нагадування"
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
    val input = "покажи групи"
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
    val input = "показати нотатку"
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
    val input = "покажи списки покупок"
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
    val input = "відкрий додаток"
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
    val input = "відкрий налаштування"
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
    val input = "відкрий налаштування гучності"
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
    val input = "відкрий допомогу"
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
    val input = "через 15 хвилин випустити реліз"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(15 * MINUTE, model?.afterMillis)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testTimerHours() {
    val input = "через 3 години випустити реліз"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(3 * HOUR, model?.afterMillis)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testTimerDays() {
    val input = "через 1 день випустити реліз"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(DAY, model?.afterMillis)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testTimerHalfHour() {
    val input = "через пів години задзвонити до дому"
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
    val input = "кожного понеділка і п'ятниці о 17 годині випустити реліз"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.WEEK, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(listOf(0, 1, 0, 0, 0, 1, 0), model?.weekdays)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testByDateEveryDay() {
    val input = "випустити реліз кожного дня о 14 30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderDecember() {
    val input = "25 грудня о 17 годині випустити реліз"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(12, 25, 17, 0), model?.dateTime)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJanuary() {
    val input = "випустити реліз п'ятого січня о 12 30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(1, 5, 12, 30), model?.dateTime)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderFebruary() {
    val input = "випустити реліз 5 лютого о сьомій годині вечора"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(2, 5, 19, 0), model?.dateTime)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderMarch() {
    val input = "випустити реліз вісімнадцятого березня о 13 годині 45 хвилин"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(3, 18, 13, 45), model?.dateTime)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderApril() {
    val input = "29 квітня об 11 випустити реліз"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(4, 29, 11, 0), model?.dateTime)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderMay() {
    val input = "випустити реліз 11 травня о 15:30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(5, 11, 15, 30), model?.dateTime)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJuneWithCall() {
    val input = "зранку 10 червня задзвонити до дому"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.CALL, model?.action)
    assertEquals("123456", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(6, 10, 7, 0), model?.dateTime)
    assertEquals(input, model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJulyWithSms() {
    val input = "першого липня о 16:33 надіслати повідомлення до дому з текстом випустити реліз"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MESSAGE, model?.action)
    assertEquals("123456", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(7, 1, 16, 33), model?.dateTime)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderAugustWithEmail() {
    val input = "25 серпня ввечері надіслати листа до дому з текстом випустити реліз"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MAIL, model?.action)
    assertEquals("test@mail.com", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(8, 25, 19, 0), model?.dateTime)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderSeptemberWithRepeat() {
    val input = "десятого вересня вночі випустити реліз повторювати кожного дня"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals(getExpectedDateTime(9, 10, 23, 0), model?.dateTime)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderOctoberWithCalendar() {
    val input = "випустити реліз 8 жовтня в день додай до календаря"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(true, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(10, 8, 12, 0), model?.dateTime)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderNovember() {
    val input = "випустити реліз 11 листопада об одинадцятій годині одинадцять хвилин"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(11, 11, 11, 11), model?.dateTime)
    assertEquals("випустити реліз", model?.summary?.lowercase())
  }
}