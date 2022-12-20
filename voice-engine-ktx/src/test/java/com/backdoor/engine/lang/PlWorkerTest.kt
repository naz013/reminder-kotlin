package com.backdoor.engine.lang

import com.backdoor.engine.Recognizer
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.ActionType
import com.backdoor.engine.misc.ContactsInterface
import com.backdoor.engine.misc.Locale
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.ZoneId

class PlWorkerTest {

  private val worker = PlWorker(ZoneId.of("GMT"))
  private val contactsInterface = mockk<ContactsInterface>()
  private val recognizer = Recognizer.Builder()
    .setLocale(Locale.PL)
    .setTimes(TIMES)
    .setTimeZone("GMT")
    .build()

  @Before
  fun setUp() {
    every { contactsInterface.findNumber(any()) }.answers { null }
    every { contactsInterface.findNumber("domu") }.answers { "123456" }
    every { contactsInterface.findEmail(any()) }.answers { null }
    every { contactsInterface.findEmail("domu") }.answers { "test@mail.com" }
    recognizer.setContactHelper(contactsInterface)
  }

  @Test
  fun testResponseYes() {
    val model = recognizer.recognize("Oczywiście, że tak")

    assertEquals(true, model != null)
    assertEquals(ActionType.ANSWER, model?.type)
    assertEquals(Action.YES, model?.action)

    val model2 = recognizer.recognize("tak")

    assertEquals(true, model2 != null)
    assertEquals(ActionType.ANSWER, model2?.type)
    assertEquals(Action.YES, model2?.action)
  }

  @Test
  fun testResponseNo() {
    val model = recognizer.recognize("nie, nie zapisuj")

    assertEquals(true, model != null)
    assertEquals(ActionType.ANSWER, model?.type)
    assertEquals(Action.NO, model?.action)

    val model2 = recognizer.recognize("nie")

    assertEquals(true, model2 != null)
    assertEquals(ActionType.ANSWER, model2?.type)
    assertEquals(Action.NO, model2?.action)
  }

  @Test
  fun testShowBirthdaysForNextWeek() {
    val input = "pokaż urodziny na następny tydzień"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.BIRTHDAYS, model?.action)
    assertEquals(DAY * 7, model?.repeatInterval)
  }

  @Test
  fun testShowRemindersForNext3Days() {
    val input = "pokaż przypomnienia na następne 3 dni"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.REMINDERS, model?.action)
    assertEquals(DAY * 3, model?.repeatInterval)
  }

  @Test
  fun testDisableReminders() {
    val input = "wyłącz wszystkie przypomnienia"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.DISABLE, model?.action)
  }

  @Test
  fun testClearArchivedReminders() {
    val input = "opróżnij kosz"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.TRASH, model?.action)
  }

  @Test
  fun testAddGroup() {
    val input = "dodaj grupę praca"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.GROUP, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("praca", model?.summary?.lowercase())
  }

  @Test
  fun testAddNote() {
    val input = "nowa notatka wypuścić nową wersję w następny wtorek"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.NOTE, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("wypuścić nową wersję w następny wtorek", model?.summary?.lowercase())
  }

  @Test
  fun testShowBirthdays() {
    val input = "pokaż urodziny"
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
    val input = "pokaż aktywne przypomnienia"
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
    val input = "pokaż przypomnienie"
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
    val input = "pokaż grupy"
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
    val input = "pokaż notatkę"
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
    val input = "pokaż listy zakupów"
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
    val input = "otwórz aplikację"
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
    val input = "Otwórz ustawienia"
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
    val input = "otwórz ustawienia głośności"
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
    val input = "otwórz pomoc"
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
    val input = "wydać nową wersję za 15 minut"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(15 * MINUTE, model?.afterMillis)
    assertEquals("wydać nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testTimerHours() {
    val input = "wydać nową wersję za 3 godziny"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(3 * HOUR, model?.afterMillis)
    assertEquals("wydać nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testTimerDays() {
    val input = "wydać nową wersję za 2 dni"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(DAY * 2, model?.afterMillis)
    assertEquals("wydać nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testTimerHalfHour() {
    val input = "zadzwoń do domu za pół godziny"
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
    val input = "wydawaj nową wersję w każdy poniedziałek i piątek o 17:00"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.WEEK, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(listOf(0, 1, 0, 0, 0, 1, 0), model?.weekdays)
    assertEquals("wydawaj nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testByDateEveryDay() {
    val input = "wydawaj nową wersję codziennie o 14 30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals("wydawaj nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testByDateEveryDay2() {
    val input = "wydawaj nową wersję każdego dnia o 14:30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals("wydawaj nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderDecember() {
    val input = "opublikuj nową wersję 25 grudnia o godzinie 17:00"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(12, 25, 17, 0), model?.dateTime)
    assertEquals("opublikuj nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJanuary() {
    val input = "opublikuj nową wersję piątego stycznia o godzinie 12 30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(1, 5, 12, 30), model?.dateTime)
    assertEquals("opublikuj nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderFebruary() {
    val input = "opublikuj nową wersję 5 lutego o godzinie siódmej wieczorem"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(2, 5, 19, 0), model?.dateTime)
    assertEquals("opublikuj nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderMarch() {
    val input = "wydać komunikat osiemnasty marca o godzinie 13:45"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(3, 18, 13, 45), model?.dateTime)
    assertEquals("wydać komunikat", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderApril() {
    val input = "29 kwietnia o godzinie 11:00 opublikować nową wersję"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(4, 29, 11, 0), model?.dateTime)
    assertEquals("opublikować nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderMay() {
    val input = "opublikować nową wersję 11 maja o 15:30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(5, 11, 15, 30), model?.dateTime)
    assertEquals("opublikować nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJuneWithCall() {
    val input = "rano 10 czerwca zadzwonić do domu"
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
    val input = "1 lipca o 16:33 wyślij wiadomość do domu z tekstem opublikuj nową wersję"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MESSAGE, model?.action)
    assertEquals("123456", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(7, 1, 16, 33), model?.dateTime)
    assertEquals("opublikuj nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderAugustWithEmail() {
    val input = "Wieczorem 25 sierpnia wyślij list do domu z treścią opublikuj nową wersję"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MAIL, model?.action)
    assertEquals("test@mail.com", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(8, 25, 19, 0), model?.dateTime)
    assertEquals("opublikuj nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderSeptemberWithRepeat() {
    val input = "wypuść nową wersję w nocy dziesiąty września i powtarzaj codziennie"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals(getExpectedDateTime(9, 10, 23, 0), model?.dateTime)
    assertEquals("wypuść nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderOctoberWithCalendar() {
    val input = "opublikuj nową wersję 8 października o godzinie 12:00 dodaj do kalendarza"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(true, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(10, 8, 12, 0), model?.dateTime)
    assertEquals("opublikuj nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderNovember() {
    val input = "opublikuj nową wersję 11 listopada o godzinie 11 11"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(11, 11, 11, 11), model?.dateTime)
    assertEquals("opublikuj nową wersję", model?.summary?.lowercase())
  }

  @Test
  fun testReplaceNumbers() {
    assertEquals(
      "w każdy wtorek pobudka o 7 rano",
      worker.replaceNumbers("w każdy wtorek pobudka o 7 rano")
    )

    assertEquals(
      "w każdy wtorek pobudka o 7.0 rano",
      worker.replaceNumbers("w każdy wtorek pobudka o siódmej rano")
    )
  }

  @Test
  fun testHasCalendar() {
    val input = "jutro o 15:40 odwiedzić lekarzy i dodać wydarzenie do kalendarza"

    assertEquals(true, worker.hasCalendar(input))

    assertEquals("jutro o 15:40 odwiedzić lekarzy i dodać wydarzenie", worker.clearCalendar(input))
  }

  @Test
  fun testHasNoCalendar() {
    val input = "w każdy wtorek pobudka o 7 rano"

    assertEquals(false, worker.hasCalendar(input))

    assertEquals("w każdy wtorek pobudka o 7 rano", worker.clearCalendar(input))
  }

  @Test
  fun testShowAction() {
    val input = "pokaż aktywne przypomnienie"

    assertEquals(true, worker.hasShowAction(input))
    assertEquals(Action.ACTIVE_REMINDERS, worker.getShowAction(input))
  }
}
