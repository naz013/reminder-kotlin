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

class EsWorkerTest {

  private val contactsInterface = mockk<ContactsInterface>()
  private val recognizer = Recognizer.Builder()
    .setLocale(Locale.ES)
    .setTimes(TIMES)
    .setTimeZone("GMT")
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
  fun testResponseYes() {
    val model = recognizer.recognize("sí, por supuesto")

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
    val input = "mostrar cumpleaños para la próxima semana"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.BIRTHDAYS, model?.action)
    assertEquals(DAY * 7, model?.repeatInterval)
  }

  @Test
  fun testShowRemindersForNext3Days() {
    val input = "mostrar recordatorios para los próximos 3 días"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.SHOW, model?.type)
    assertEquals(Action.REMINDERS, model?.action)
    assertEquals(DAY * 3, model?.repeatInterval)
  }

  @Test
  fun testDisableReminders() {
    val input = "desactivar todos los recordatorios"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.DISABLE, model?.action)
  }

  @Test
  fun testClearArchivedReminders() {
    val input = "limpiar la basura"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.TRASH, model?.action)
  }

  @Test
  fun testEmptyArchivedReminders() {
    val input = "papelera vacía"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.ACTION, model?.type)
    assertEquals(Action.TRASH, model?.action)
  }

  @Test
  fun testAddGroup() {
    val input = "crear trabajo en grupo"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.GROUP, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("trabajo", model?.summary?.lowercase())
  }

  @Test
  fun testAddGroup2() {
    val input = "añadir trabajo en grupo"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.GROUP, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("trabajo", model?.summary?.lowercase())
  }

  @Test
  fun testAddGroup3() {
    val input = "añadir nuevo trabajo en grupo"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.GROUP, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("trabajo", model?.summary?.lowercase())
  }

  @Test
  fun testAddNote() {
    val input = "se publica la actualización de crear una nueva nota"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.NOTE, model?.type)
    assertEquals(Action.NONE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(0L, model?.afterMillis)
    assertEquals("se publica la actualización", model?.summary?.lowercase())
  }

  @Test
  fun testShowBirthdays() {
    val input = "mostrar cumpleaños"
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
  fun testShowBirthdays2() {
    val input = "mostrar los cumpleaños"
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
    val input = "mostrar recordatorios activos"
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
    val input = "mostrar recordatorios"
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
    val input = "mostrar grupos"
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
    val input = "Mostrar notas"
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
    val input = "mostrar listas de compras"
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
    val input = "Aplicación abierta"
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
    val input = "configuración abierta"
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
    val input = "cambiar la configuración de volumen"
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
    val input = "configuración de volumen abierto"
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
    val input = "abrir ayuda"
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
    val input = "después de 15 minutos abra la aplicación"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(15 * MINUTE, model?.afterMillis)
    assertEquals("abra la aplicación", model?.summary?.lowercase())
  }

  @Test
  fun testTimerHours() {
    val input = "después de 3 horas sal a caminar"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(3 * HOUR, model?.afterMillis)
    assertEquals("sal a caminar", model?.summary?.lowercase())
  }

  @Test
  fun testTimerDays() {
    val input = "después de 1 día de aplicación de liberación"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(DAY, model?.afterMillis)
    assertEquals("de aplicación de liberación", model?.summary?.lowercase())
  }

  @Test
  fun testTimerHalfHour() {
    val input = "después de media hora llamar a casa"
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
    val input = "pruebas de control todos los lunes y viernes a las 19"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.WEEK, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(listOf(0, 1, 0, 0, 0, 1, 0), model?.weekdays)
    assertEquals("pruebas de control", model?.summary?.lowercase())
  }

  @Test
  fun testByDateEveryDay() {
    val input = "pruebas de control todos los días a las 14 30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals("pruebas de control", model?.summary?.lowercase())
  }

  @Test
  fun testByDateEveryDay2() {
    val input = "comprobar las pruebas todos los días a las 14:30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals("comprobar las pruebas", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderDecember() {
    val input = "actualización de lanzamiento el 25 de diciembre a las 17 en punto"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(12, 25, 17, 0), model?.dateTime)
    assertEquals("actualización de lanzamiento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJanuary() {
    val input = "actualización de lanzamiento el 2 de enero a las 12 30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(1, 2, 12, 30), model?.dateTime)
    assertEquals("actualización de lanzamiento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderFebruary() {
    val input = "actualización de lanzamiento el cinco de febrero a las siete en punto"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(2, 5, 7, 0), model?.dateTime)
    assertEquals("actualización de lanzamiento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderMarch() {
    val input = "actualización de lanzamiento el dieciocho de marzo a las 13 45"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(3, 18, 13, 45), model?.dateTime)
    assertEquals("actualización de lanzamiento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderApril() {
    val input = "el 29 de abril a las 11 actualización de lanzamiento"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(4, 29, 11, 0), model?.dateTime)
    assertEquals("actualización de lanzamiento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderMay() {
    val input = "actualización de lanzamiento el 11 de mayo a las 15:30"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(5, 11, 15, 30), model?.dateTime)
    assertEquals("actualización de lanzamiento", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderJuneWithCall() {
    val input = "el 10 de junio por la mañana llamar a casa"
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
    val input = "el primero de julio a las 16:33 envía un mensaje a casa con el texto run forest run"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MESSAGE, model?.action)
    assertEquals("123456", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(7, 1, 16, 33), model?.dateTime)
    assertEquals("run forest run", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderAugustWithEmail() {
    val input = "el 25 de agosto por la tarde enviar correo electrónico a casa con texto ejecutar pruebas"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MAIL, model?.action)
    assertEquals("test@mail.com", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(8, 25, 19, 0), model?.dateTime)
    assertEquals("ejecutar pruebas", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderAugustWithLetter() {
    val input = "el 20 de agosto por la mañana enviar carta a casa con texto ejecutar pruebas"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.MAIL, model?.action)
    assertEquals("test@mail.com", model?.target)
    assertEquals(false, model?.hasCalendar)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(8, 20, 7, 0), model?.dateTime)
    assertEquals("ejecutar pruebas", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderSeptemberWithRepeat() {
    val input = "el diez de septiembre por la noche, actualice y repita todos los días"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(DAY, model?.repeatInterval)
    assertEquals(getExpectedDateTime(9, 10, 23, 0), model?.dateTime)
    assertEquals("actualice y repita", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderOctoberWithCalendar() {
    val input = "lance la actualización el 8 de octubre al mediodía y agréguela al calendario"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(true, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(10, 8, 12, 0), model?.dateTime)
    assertEquals("lance la actualización", model?.summary?.lowercase())
  }

  @Test
  fun testByDateReminderNovember() {
    val input = "actualización de lanzamiento el 11 de noviembre a las once en punto"
    val model = recognizer.recognize(input)

    assertEquals(true, model != null)
    assertEquals(ActionType.REMINDER, model?.type)
    assertEquals(Action.DATE, model?.action)
    assertEquals(false, model?.hasCalendar)
    assertEquals(null, model?.target)
    assertEquals(0L, model?.repeatInterval)
    assertEquals(getExpectedDateTime(11, 11, 11, 0), model?.dateTime)
    assertEquals("actualización de lanzamiento", model?.summary?.lowercase())
  }
}
