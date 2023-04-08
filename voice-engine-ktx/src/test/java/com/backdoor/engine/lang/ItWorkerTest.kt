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

class ItWorkerTest {

  private val contactsInterface = mockk<ContactsInterface>()
  private val recognizer = Recognizer.Builder()
    .setLocale(Locale.IT)
    .setTimes(TIMES)
    .setTimeZone(TIME_ZONE_ID)
    .build()

  @Before
  fun setUp() {
    every { contactsInterface.findNumber(any()) }.answers { null }
    every { contactsInterface.findNumber("casa") }.answers { "123456" }
    every { contactsInterface.findEmail(any()) }.answers { null }
    every { contactsInterface.findEmail("casa") }.answers { "test@mail.com" }
    recognizer.setContactHelper(contactsInterface)
  }

  @Test
  fun testAfterTomorrow() {
    val input = "dopodomani alle 19 richiesta rilascio"
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
    assertEquals("richiesta rilascio", model?.summary?.lowercase())
  }

  @Test
  fun testTomorrow() {
    val input = "domani alle 19 richiesta di rilascio"
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
    assertEquals("richiesta di rilascio", model?.summary?.lowercase())
  }

  @Test
  fun testResponseYes() {
    val model = recognizer.recognize("si, certo")

    assertEquals(true, model != null)
    assertEquals(ActionType.ANSWER, model?.type)
    assertEquals(Action.YES, model?.action)

    val model2 = recognizer.recognize("si")

    assertEquals(true, model2 != null)
    assertEquals(ActionType.ANSWER, model2?.type)
    assertEquals(Action.YES, model2?.action)
  }

  @Test
  fun testResponseNo() {
    val model = recognizer.recognize("no, no guardes")

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
    val input = "mostra i compleanni per la prossima settimana"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.BIRTHDAYS, model?.action)
    assertEquals(DAY * 7, model?.repeatInterval)
  }

  @Test
  fun testShowRemindersForNext3Days() {
    val input = "mostra promemoria per i prossimi 3 giorni"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.REMINDERS, model?.action)
    assertEquals(DAY * 3, model?.repeatInterval)
  }

  @Test
  fun testDisableReminders() {
    recognizer.recognize("disattivare tutti i promemoria").also { model ->
      assertEquals(true, model != null)
      assertEquals(ActionType.ACTION, model?.type)
      assertEquals(Action.DISABLE, model?.action)
    }

    recognizer.recognize("disabilita tutti i promemoria").also { model ->
      assertEquals(true, model != null)
      assertEquals(ActionType.ACTION, model?.type)
      assertEquals(Action.DISABLE, model?.action)
    }
  }

  @Test
  fun testClearArchivedReminders() {
    recognizer.recognize("pulire la spazzatura").also { model ->
      assertEquals(true, model != null)
      assertEquals(ActionType.ACTION, model?.type)
      assertEquals(Action.TRASH, model?.action)
    }

    recognizer.recognize("spazzatura vuota").also { model ->
      assertEquals(true, model != null)
      assertEquals(ActionType.ACTION, model?.type)
      assertEquals(Action.TRASH, model?.action)
    }
  }

  @Test
  fun testEmptyArchivedReminders() {
    recognizer.recognize("bidone della spazzatura vuoto").also { model ->
      assertEquals(true, model != null)
      assertEquals(ActionType.ACTION, model?.type)
      assertEquals(Action.TRASH, model?.action)
    }

    recognizer.recognize("spazzatura chiara").also { model ->
      assertEquals(true, model != null)
      assertEquals(ActionType.ACTION, model?.type)
      assertEquals(Action.TRASH, model?.action)
    }
  }

  @Test
  fun testAddGroup() {
    val input = "creare un lavoro di gruppo"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.GROUP, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("un lavoro", model?.summary?.lowercase())
  }

  @Test
  fun testAddGroup2() {
    val input = "aggiungere il lavoro di gruppo"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.GROUP, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("il lavoro", model?.summary?.lowercase())
  }

  @Test
  fun testAddGroup3() {
    val input = "aggiungere un nuovo lavoro di gruppo"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.GROUP, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("un lavoro", model?.summary?.lowercase())
  }

  @Test
  fun testAddNote() {
    val input = "aggiornamento viene pubblicato creare una nuova nota"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.NOTE, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("aggiornamento viene pubblicato", model?.summary?.lowercase())
  }

  @Test
  fun testShowBirthdays() {
    val input = "mostra compleanni"
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
  fun testShowBirthdays_2() {
    val input = "mostra il compleanno"
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
    val input = "mostra promemoria attivi"
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
    val input = "mostra promemoria"
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
    val input = "gruppi di spettacoli"
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
    val input = "mostra le note"
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
    val input = "mostra le liste della spesa"
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
    val input = "applicazione aperta"
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
    val input = "configurazione aperta"
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
    val input = "modificare le impostazioni del volume"
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
  fun testOpenVolumeSettings2() {
    val input = "aprire l'impostazione del volume"
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
    val input = "aiuto aperto"
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
    val input = "dopo 15 minuti apri l'app"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(15 * MINUTE, model?.afterMillis)
    assertEquals("apri l'app", model?.summary?.lowercase())
  }

  @Test
  fun testTimerHours() {
    val input = "dopo 3 ore vai a fare una passeggiata"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(3 * HOUR, model?.afterMillis)
    assertEquals("vai a fare una passeggiata", model?.summary?.lowercase())
  }

  @Test
  fun testTimerDays() {
    val input = "dopo 1 giorno dal rilascio dell'applicazione"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(DAY, model?.afterMillis)
    assertEquals("dal rilascio dell'applicazione", model?.summary?.lowercase())
  }

  @Test
  fun testTimerHalfHour() {
    val input = "dopo mezz'ora telefona a casa"
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
    val input = "test di controllo ogni lunedì e venerdì alle 19:00"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.WEEK, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(listOf(0, 1, 0, 0, 0, 1, 0), model?.weekdays)
    assertEquals("test di controllo", model?.summary?.lowercase())
  }

  @Test
  fun testByDateEveryDay() {
    val input = "test di controllo tutti i giorni alle 14 30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals("test di controllo", model?.summary?.lowercase())
  }

  @Test
  fun testByDateEveryDay2() {
    val input = "controllare i test ogni giorno alle 14:30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals("controllare i test", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderDecember() {
    val input = "rilascio aggiornamento il 25 dicembre alle ore 17"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(12, 25, 17, 0), model?.dateTime)
    assertEquals("rilascio aggiornamento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJanuary() {
    val input = "rilascio aggiornamento il 2 gennaio alle 12 30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(1, 2, 12, 30), model?.dateTime)
    assertEquals("rilascio aggiornamento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderFebruary() {
    val input = "rilascio aggiornamento il 5 febbraio alle sette"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(2, 5, 7, 0), model?.dateTime)
    assertEquals("rilascio aggiornamento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderMarch() {
    val input = "rilascio aggiornamento il 18 marzo alle 13 45"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(3, 18, 13, 45), model?.dateTime)
    assertEquals("rilascio aggiornamento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderApril() {
    val input = "il 29 aprile alle 11 rilascio dell'aggiornamento"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(4, 29, 11, 0), model?.dateTime)
    assertEquals("rilascio dell'aggiornamento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderMay() {
    val input = "rilascio aggiornamento l'11 maggio alle 15:30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(5, 11, 15, 30), model?.dateTime)
    assertEquals("rilascio aggiornamento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJuneWithCall() {
    val input = "10 giugno mattina chiamata a casa"
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
    val input = "il primo luglio alle 16:33 invia un messaggio a casa con il testo run forest run"
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
    val input = "1 luglio alle 16:33 invia un messaggio a casa con il messaggio sì, sono venuto"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MESSAGE, model?.action)
    assertEquals("123456", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(7, 1, 16, 33), model?.dateTime)
    assertEquals("sì, sono venuto", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderAugustWithEmail() {
    val input = "la sera del 25 agosto inviare mail a casa con test di esecuzione testo"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MAIL, model?.action)
    assertEquals("test@mail.com", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(8, 25, 19, 0), model?.dateTime)
    assertEquals("con test di esecuzione", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderAugustWithLetter() {
    val input = "il 20 agosto al mattino spedisci lettera a casa con test di esecuzione del testo"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MAIL, model?.action)
    assertEquals("test@mail.com", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(8, 20, 7, 0), model?.dateTime)
    assertEquals("con test di esecuzione", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderSeptemberWithRepeat() {
    val input = "il 10 settembre di notte, aggiorna e ripeti ogni giorno"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(9, 10, 23, 0), model?.dateTime)
    assertEquals("aggiorna e ripeti", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderOctoberWithCalendar() {
    val input = "rilasciare l'aggiornamento l'8 ottobre a mezzogiorno e aggiungerlo al calendario"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(true, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(10, 8, 12, 0), model?.dateTime)
    assertEquals("rilasciare l'aggiornamento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderNovember() {
    val input = "rilascio aggiornamento l'11 novembre alle undici"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getDateTimeWithShiftedYearIfNeeded(11, 11, 11, 0), model?.dateTime)
    assertEquals("rilascio aggiornamento", model?.summary?.lowercase())
  }
}
