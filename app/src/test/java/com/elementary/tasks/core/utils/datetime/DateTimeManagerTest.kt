package com.elementary.tasks.core.utils.datetime

class DateTimeManagerTest {

//  private val dateTimePreferences = mockk<DateTimePreferences>()
//  private val nowDateTimeProvider = mockk<NowDateTimeProvider>()
//  private val birthday = mockk<Birthday>()
//  private val dateTimeManager = DateTimeManager(nowDateTimeProvider, dateTimePreferences)
//  private val oldTimeUtil = OldTimeUtil()
//
//  @Before
//  fun setUp() {
//    mockkObject(Language)
//
//    every { dateTimePreferences.locale } returns Locale.US
//    every { dateTimePreferences.is24HourFormat } returns true
//
//    every { nowDateTimeProvider.nowDate() } returns LocalDate.now()
//    every { nowDateTimeProvider.nowDateTime() } returns LocalDateTime.now()
//    every { nowDateTimeProvider.nowTime() } returns LocalTime.now()
//
//    every { birthday.ignoreYear } returns false
//    every { birthday.showedYear } returns 0
//  }
//
//  @Test
//  fun givenBirthdayDate_thenCalculateFutureBirthdayDate() {
//    val nowDate = LocalDate.of(2023, 5, 8)
//    val nowTime = LocalTime.of(15, 0)
//
//    val time = LocalTime.of(12, 0)
//    val date = LocalDate.of(1994, 6, 17)
//
//    val nowDateTime = LocalDateTime.of(nowDate, nowTime)
//
//    every { nowDateTimeProvider.nowDate() } returns nowDate
//    every { nowDateTimeProvider.nowDateTime() } returns nowDateTime
//
//    val result = dateTimeManager.getFutureBirthdayDate(
//      birthdayTime = time,
//      birthdayDate = date,
//      nowDateTime = nowDateTime,
//      birthday = birthday
//    )
//
//    val expected = LocalDateTime.of(LocalDate.of(2023, 6, 17), time)
//    assertEquals(expected, result)
//  }
//
//  @Test
//  fun givenBirthdayDate_andNowDateWasInPast_thenCalculateFutureBirthdayDateInPast() {
//    val nowDate = LocalDate.of(2020, 6, 17)
//    val nowTime = LocalTime.of(12, 0)
//
//    val time = LocalTime.of(12, 0)
//    val date = LocalDate.of(1994, 6, 17)
//
//    val nowDateTime = LocalDateTime.of(nowDate, nowTime)
//
//    every { nowDateTimeProvider.nowDate() } returns nowDate
//    every { nowDateTimeProvider.nowDateTime() } returns nowDateTime
//
//    val result = dateTimeManager.getFutureBirthdayDate(
//      birthdayTime = time,
//      birthdayDate = date,
//      nowDateTime = nowDateTime,
//      birthday = birthday
//    )
//
//    val expected = LocalDateTime.of(LocalDate.of(2020, 6, 17), time)
//    assertEquals(expected, result)
//  }
//
//  @Test
//  fun givenBirthdayDateWithYearIgnoreFlag_thenCalculateFutureBirthdayDate() {
//    val nowDate = LocalDate.of(2023, 5, 8)
//    val nowTime = LocalTime.of(15, 0)
//
//    val time = LocalTime.of(12, 0)
//    val date = LocalDate.of(2022, 7, 17)
//
//    val nowDateTime = LocalDateTime.of(nowDate, nowTime)
//
//    every { nowDateTimeProvider.nowDate() } returns nowDate
//    every { nowDateTimeProvider.nowDateTime() } returns nowDateTime
//
//    val result = dateTimeManager.getFutureBirthdayDate(
//      birthdayTime = time,
//      birthdayDate = date,
//      nowDateTime = nowDateTime,
//      birthday = birthday
//    )
//
//    val expected = LocalDateTime.of(LocalDate.of(2023, 7, 17), time)
//    assertEquals(expected, result)
//  }
//
//  @Test
//  fun givenBirthdayDate_isNull_thenReadableShouldBeEmpty() {
//    val result = dateTimeManager.getReadableBirthDate(null, false)
//    assertEquals("", result)
//  }
//
//  @Test
//  fun givenBirthdayDate_thenReadableShouldBeCorrect() {
//    val date = LocalDate.of(1994, 6, 17)
//
//    val result = dateTimeManager.getReadableBirthDate(date, false)
//
//    assertEquals("17 June 1994", result)
//  }
//
//  @Test
//  fun givenBirthdayDate_andIgnoreYearIsTrue_thenReadableShouldNotHaveAYear() {
//    val date = LocalDate.of(1994, 6, 17)
//
//    val result = dateTimeManager.getReadableBirthDate(date, true)
//
//    assertEquals("17 June", result)
//  }
//
//  @Test
//  fun givenRawBirthdayDate_thenShouldParseCorrectly() {
//    val formatted = "1995-12-23"
//    val localDate = dateTimeManager.parseBirthdayDate(formatted)
//
//    assertEquals(LocalDate.of(1995, 12, 23), localDate)
//    assertEquals(formatted, dateTimeManager.formatBirthdayDate(localDate!!))
//  }
//
//  @Test
//  fun givenRawBirthdayDate_thenReturnFormattedAge() {
//    val expectedAge = "24 years"
//    whenever(textProvider.getText(any(), anyVararg())).thenReturn(expectedAge)
//
//    val formatted = "1995-12-23"
//    val nowDate = LocalDate.of(2020, 6, 15)
//    val formattedAge = dateTimeManager.getAgeFormatted(
//      date = formatted,
//      nowDate = nowDate
//    )
//
//    com.nhaarman.mockitokotlin2.verify(textProvider, times(1))
//      .getText(any(), anyVararg())
//    assertEquals(expectedAge, formattedAge)
//  }
//
//  @Test
//  fun givenDateTime_thenConvertItToMillis() {
//    val calendar = Calendar.getInstance()
//    calendar.timeInMillis = System.currentTimeMillis()
//    calendar.set(2022, 11, 25, 15, 15, 15)
//    calendar.set(Calendar.MILLISECOND, 0)
//
//    val dateTime = LocalDateTime.of(2022, 12, 25, 15, 15, 15)
//
//    assertEquals(
//      calendar.timeInMillis,
//      dateTimeManager.toMillis(dateTime)
//    )
//  }
//
//  @Test
//  fun givenMillis_thenConvertItToLocalDateTime() {
//    val calendar = Calendar.getInstance()
//    calendar.timeInMillis = System.currentTimeMillis()
//    calendar.set(2022, 11, 25, 15, 15, 15)
//    calendar.set(Calendar.MILLISECOND, 0)
//
//    val dateTime = LocalDateTime.of(2022, 12, 25, 15, 15, 15)
//    val millis = dateTimeManager.toMillis(dateTime)
//
//    assertEquals(
//      calendar.timeInMillis,
//      millis
//    )
//
//    assertEquals(
//      dateTime,
//      dateTimeManager.fromMillis(millis)
//    )
//  }
//
//  // region begin Remaining
//
//  @Test
//  fun givenFutureBirthdayDateTime_andIgnoreYearIsFalse_thenReturnRemainingString() {
//    val futureBirthdayDateTime = LocalDateTime.of(2023, 9, 10, 12, 0, 0)
//    val nowDateTime = LocalDateTime.of(2023, 8, 10, 12, 0, 0)
//
//    val expected = "1 month"
//
//    whenever(textProvider.getText(any(), anyVararg())).thenReturn(expected)
//    whenever(textProvider.getText(any())).thenReturn("not")
//
//    val result = dateTimeManager.getBirthdayRemaining(
//      futureBirthdayDateTime = futureBirthdayDateTime,
//      ignoreYear = false,
//      nowDateTime = nowDateTime
//    )
//
//    assertEquals(expected, result)
//  }
//
//  @Test
//  fun givenFutureBirthdayDateTime_andIgnoreYearIsTrue_thenReturnRemainingStringAsNull() {
//    val futureBirthdayDateTime = LocalDateTime.of(2023, 9, 10, 12, 0, 0)
//    val nowDateTime = LocalDateTime.of(2023, 8, 10, 12, 0, 0)
//
//    val result = dateTimeManager.getBirthdayRemaining(
//      futureBirthdayDateTime = futureBirthdayDateTime,
//      ignoreYear = true,
//      nowDateTime = nowDateTime
//    )
//
//    assertNull(result)
//  }
//
//  @Test
//  fun givenFutureBirthdayDateTime_andNowDateTimeIsTheSame_thenReturnRemainingStringAsNull() {
//    val futureBirthdayDateTime = LocalDateTime.of(2023, 9, 10, 12, 0, 0, 0)
//    val nowDateTime = LocalDateTime.of(2023, 9, 10, 12, 0, 0, 0)
//
//    val result = dateTimeManager.getBirthdayRemaining(
//      futureBirthdayDateTime = futureBirthdayDateTime,
//      ignoreYear = false,
//      nowDateTime = nowDateTime
//    )
//
//    assertNull(result)
//  }
//
//  // end region
//
//  @Test
//  fun testToGmtFormat() {
//    val millis = getMillis(2022, 12, 25, 15, 15, 15)
//    val localDateTime = getDateTime(2022, 12, 25, 15, 15, 15)
//
//    assertEquals(toGmt(millis), dateTimeManager.getGmtFromDateTime(localDateTime))
//  }
//
//  @Test
//  fun testGetNextMonthDayTime() {
//    val dayOfMonth = 29
//    val startTime = getDateTime(2022, 12, dayOfMonth, 14, 30)
//    val reminder = Reminder(
//      dayOfMonth = dayOfMonth,
//      remindBefore = 0,
//      eventTime = dateTimeManager.getGmtFromDateTime(startTime),
//      repeatInterval = 1
//    )
//
//    val nowDateTime = getDateTime(2022, 12, dayOfMonth, 14, 30)
//    val calculatedDateTime = dateTimeManager.getNewNextMonthDayTime(reminder, nowDateTime)
//
//    val nowMillis = getMillis(2022, 12, dayOfMonth, 14, 30)
//    val calculatedMillis = oldTimeUtil.getNextMonthDayTime(reminder, nowMillis)
//
//    assertEquals(toGmt(calculatedMillis), dateTimeManager.getGmtFromDateTime(calculatedDateTime))
//  }
//
//  @Test
//  fun testGetNextMonthDayTime2() {
//    val dayOfMonth = 14
//    val startTime = getDateTime(2022, 12, dayOfMonth, 14, 30)
//    val reminder = Reminder(
//      dayOfMonth = dayOfMonth,
//      remindBefore = 0,
//      eventTime = dateTimeManager.getGmtFromDateTime(startTime),
//      repeatInterval = 2
//    )
//
//    val nowDateTime = getDateTime(2022, 12, dayOfMonth, 14, 30)
//    val calculatedDateTime = dateTimeManager.getNewNextMonthDayTime(reminder, nowDateTime)
//
//    val nowMillis = getMillis(2022, 12, dayOfMonth, 14, 30)
//    val calculatedMillis = oldTimeUtil.getNextMonthDayTime(reminder, nowMillis)
//
//    assertEquals(toGmt(calculatedMillis), dateTimeManager.getGmtFromDateTime(calculatedDateTime))
//  }
//
//  @Test
//  fun testGetNextMonthDayTimeLastDay() {
//    var reminder = Reminder(
//      dayOfMonth = 0,
//      remindBefore = 0,
//      eventTime = dateTimeManager.getGmtFromDateTime(getDateTime(2022, 12, 31, 14, 30)),
//      repeatInterval = 1
//    )
//
//    var calculatedDateTime = dateTimeManager.getNewNextMonthDayTime(
//      reminder,
//      getDateTime(2022, 12, 31, 14, 30)
//    )
//    reminder = reminder.copy(eventTime = dateTimeManager.getGmtFromDateTime(calculatedDateTime))
//    assertEquals(
//      dateTimeManager.getGmtFromDateTime(getDateTime(2023, 1, 31, 14, 30)),
//      reminder.eventTime
//    )
//
//    calculatedDateTime = dateTimeManager.getNewNextMonthDayTime(
//      reminder,
//      getDateTime(2023, 1, 31, 14, 30)
//    )
//    reminder = reminder.copy(eventTime = dateTimeManager.getGmtFromDateTime(calculatedDateTime))
//    assertEquals(
//      dateTimeManager.getGmtFromDateTime(getDateTime(2023, 2, 28, 14, 30)),
//      reminder.eventTime
//    )
//  }
//
//  private fun getDateTime(
//    year: Int,
//    month: Int,
//    day: Int,
//    hour: Int,
//    minute: Int,
//    second: Int = 0
//  ): LocalDateTime {
//    return LocalDateTime.of(year, month, day, hour, minute, second)
//  }
//
//  private fun getMillis(
//    year: Int,
//    month: Int,
//    day: Int,
//    hour: Int,
//    minute: Int,
//    second: Int = 0
//  ): Long {
//    val calendar = Calendar.getInstance()
//    calendar.timeInMillis = System.currentTimeMillis()
//    calendar.set(year, month - 1, day, hour, minute, second)
//    calendar.set(Calendar.MILLISECOND, 0)
//    return calendar.timeInMillis
//  }
//
//  private fun toGmt(millis: Long): String {
//    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)
//    format.timeZone = TimeZone.getTimeZone("GMT")
//    return try {
//      format.format(Date(millis))
//    } catch (e: Throwable) {
//      ""
//    }
//  }
}
