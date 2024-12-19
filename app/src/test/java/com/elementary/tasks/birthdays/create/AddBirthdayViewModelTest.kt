package com.elementary.tasks.birthdays.create

import android.net.Uri
import com.elementary.tasks.BaseTest
import com.github.naz013.analytics.AnalyticsEventSender
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.os.IntentDataHolder
import com.elementary.tasks.core.os.contacts.ContactsReader
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.UriReader
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.getOrAwaitValue
import com.elementary.tasks.mockDispatcherProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate

class AddBirthdayViewModelTest : BaseTest() {

  private lateinit var viewModel: AddBirthdayViewModel

  private val birthdaysDao = mockk<BirthdaysDao>()
  private val workerLauncher = mockk<WorkerLauncher>()
  private val notifier = mockk<Notifier>()
  private val contactsReader = mockk<ContactsReader>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>()
  private val uriReader = mockk<UriReader>()
  private val updatesHelper = mockk<UpdatesHelper>()
  private val intentDataHolder = mockk<IntentDataHolder>()

  private val uiBirthdayEditAdapter = UiBirthdayEditAdapter()

  override fun setUp() {
    super.setUp()
    every { dateTimeManager.getCurrentDate() }.returns(LocalDate.now())

    viewModel = AddBirthdayViewModel(
      id = ID,
      birthdaysDao = birthdaysDao,
      dispatcherProvider = mockDispatcherProvider(),
      workerLauncher = workerLauncher,
      notifier = notifier,
      contactsReader = contactsReader,
      dateTimeManager = dateTimeManager,
      analyticsEventSender = analyticsEventSender,
      uiBirthdayEditAdapter = uiBirthdayEditAdapter,
      uriReader = uriReader,
      updatesHelper = updatesHelper,
      intentDataHolder = intentDataHolder,
      uiBirthdayDateFormatter = UiBirthdayDateFormatter(dateTimeManager)
    )
  }

  @Test
  fun testInitLoad_doNothing() = runTest {
    every { birthdaysDao.getById(ID) }.returns(null)

    viewModel.load()

    verify(exactly = 1) { birthdaysDao.getById(ID) }

    assertEquals(null, viewModel.birthday.getOrAwaitValue())
    assertEquals(null, viewModel.formattedDate.getOrAwaitValue())
    assertEquals(false, viewModel.isEdited)
    assertEquals(false, viewModel.isFromFile)
    assertEquals(false, viewModel.hasSameInDb)
  }

  @Test
  fun testCreateBirthday_activityStarted() = runTest {
    val date = LocalDate.now()
    val formattedDate = "AAA"

    every { birthdaysDao.getById(ID) }.returns(null)
    every { dateTimeManager.formatBirthdayFullDateForUi(date) }.returns(formattedDate)

    viewModel.formattedDate.observeForever { }
    viewModel.isContactAttached.observeForever { }
    viewModel.birthday.observeForever { }

    viewModel.load()
    viewModel.onContactAttached(false)
    viewModel.onDateChanged(date)

    verify(exactly = 1) { birthdaysDao.getById(eq(ID)) }

    assertEquals(false, viewModel.isEdited)
    assertEquals(false, viewModel.isFromFile)
    assertEquals(false, viewModel.hasSameInDb)
    assertEquals(date, viewModel.selectedDate)
    assertEquals(formattedDate, viewModel.formattedDate.value)
    assertEquals(false, viewModel.isContactAttached.value)
    assertEquals(null, viewModel.birthday.value)
  }

  @Test
  fun testEditBirthday_fromId() = runTest {
    val date = LocalDate.now()
    val formattedDate = "AAA"
    val name = "Name"
    val number = ""
    val birthdayDate = "1999-10-01"

    val birthday = mockk<Birthday>()
    every { birthday.uuId }.returns(ID)
    every { birthday.date }.returns(birthdayDate)
    every { birthday.name }.returns(name)
    every { birthday.number }.returns(number)
    every { birthday.ignoreYear }.returns(false)

    val expectedToEdit = uiBirthdayEditAdapter.convert(birthday)

    every { birthdaysDao.getById(ID) }.returns(birthday)
    every { dateTimeManager.parseBirthdayDate(birthdayDate) }.returns(date)
    every { dateTimeManager.formatBirthdayFullDateForUi(date) }.returns(formattedDate)

    viewModel.birthday.observeForever { }
    viewModel.formattedDate.observeForever { }

    viewModel.load()
    viewModel.onContactAttached(false)

    verify(exactly = 1) { birthdaysDao.getById(eq(ID)) }

    assertEquals(true, viewModel.isEdited)
    assertEquals(false, viewModel.isFromFile)
    assertEquals(false, viewModel.hasSameInDb)
    assertEquals(date, viewModel.selectedDate)
    assertEquals(formattedDate, viewModel.formattedDate.value)
    assertEquals(expectedToEdit, viewModel.birthday.value)
  }

  @Test
  fun testEditBirthday_fromObject() = runTest {
    val objectId = "1"
    val date = LocalDate.now()
    val formattedDate = "AAA"
    val name = "Name"
    val number = ""
    val birthdayDate = "1999-10-01"

    val birthdayObject = mockk<Birthday>()
    every { birthdayObject.uuId }.returns(objectId)
    every { birthdayObject.date }.returns(birthdayDate)
    every { birthdayObject.name }.returns(name)
    every { birthdayObject.number }.returns(number)
    every { birthdayObject.ignoreYear }.returns(false)

    val expectedToEdit = uiBirthdayEditAdapter.convert(birthdayObject)

    every { birthdaysDao.getById(ID) }.returns(null)
    every { birthdaysDao.getById(objectId) }.returns(null)
    every { dateTimeManager.parseBirthdayDate(birthdayDate) }.returns(date)
    every { dateTimeManager.formatBirthdayFullDateForUi(date) }.returns(formattedDate)

    every { intentDataHolder.get(any(), Birthday::class.java) }.returns(birthdayObject)

    viewModel.birthday.observeForever { }
    viewModel.formattedDate.observeForever { }

    viewModel.load()
    viewModel.onContactAttached(false)
    viewModel.onIntent()

    verify(exactly = 2) { birthdaysDao.getById(any()) }

    assertEquals(true, viewModel.isEdited)
    assertEquals(true, viewModel.isFromFile)
    assertEquals(false, viewModel.hasSameInDb)
    assertEquals(date, viewModel.selectedDate)
    assertEquals(formattedDate, viewModel.formattedDate.value)
    assertEquals(expectedToEdit, viewModel.birthday.value)
  }

  @Test
  fun testEditBirthday_fromObject_sameId() = runTest {
    val objectId = "1"
    val date = LocalDate.now()
    val formattedDate = "AAA"
    val name = "Name"
    val number = ""
    val birthdayDate = "1999-10-01"

    val birthdayObject = mockk<Birthday>()
    every { birthdayObject.uuId }.returns(objectId)
    every { birthdayObject.date }.returns(birthdayDate)
    every { birthdayObject.name }.returns(name)
    every { birthdayObject.number }.returns(number)
    every { birthdayObject.ignoreYear }.returns(false)

    val expectedToEdit = uiBirthdayEditAdapter.convert(birthdayObject)

    every { birthdaysDao.getById(ID) }.returns(null)
    every { birthdaysDao.getById(objectId) }.returns(birthdayObject)
    every { dateTimeManager.parseBirthdayDate(birthdayDate) }.returns(date)
    every { dateTimeManager.formatBirthdayFullDateForUi(date) }.returns(formattedDate)

    every { intentDataHolder.get(any(), Birthday::class.java) }.returns(birthdayObject)

    viewModel.birthday.observeForever { }
    viewModel.formattedDate.observeForever { }

    viewModel.load()
    viewModel.onContactAttached(false)
    viewModel.onIntent()

    verify(exactly = 2) { birthdaysDao.getById(any()) }

    assertEquals(true, viewModel.isEdited)
    assertEquals(true, viewModel.isFromFile)
    assertEquals(true, viewModel.hasSameInDb)
    assertEquals(date, viewModel.selectedDate)
    assertEquals(formattedDate, viewModel.formattedDate.value)
    assertEquals(expectedToEdit, viewModel.birthday.value)
  }

  @Test
  fun testEditBirthday_fromUri() = runTest {
    val objectId = "1"
    val date = LocalDate.now()
    val formattedDate = "AAA"
    val name = "Name"
    val number = ""
    val birthdayDate = "1999-10-01"

    val uri = mockk<Uri>()

    val birthdayObject = mockk<Birthday>()
    every { birthdayObject.uuId }.returns(objectId)
    every { birthdayObject.date }.returns(birthdayDate)
    every { birthdayObject.name }.returns(name)
    every { birthdayObject.number }.returns(number)
    every { birthdayObject.ignoreYear }.returns(false)

    val expectedToEdit = uiBirthdayEditAdapter.convert(birthdayObject)

    every { birthdaysDao.getById(ID) }.returns(null)
    every { birthdaysDao.getById(objectId) }.returns(null)
    every { dateTimeManager.parseBirthdayDate(birthdayDate) }.returns(date)
    every { dateTimeManager.formatBirthdayFullDateForUi(date) }.returns(formattedDate)
    every { uriReader.readBirthdayObject(uri) }.returns(birthdayObject)

    viewModel.birthday.observeForever { }
    viewModel.formattedDate.observeForever { }

    viewModel.load()
    viewModel.onContactAttached(false)
    viewModel.onFile(uri)

    verify(exactly = 2) { birthdaysDao.getById(any()) }

    assertEquals(true, viewModel.isEdited)
    assertEquals(true, viewModel.isFromFile)
    assertEquals(false, viewModel.hasSameInDb)
    assertEquals(date, viewModel.selectedDate)
    assertEquals(formattedDate, viewModel.formattedDate.value)
    assertEquals(expectedToEdit, viewModel.birthday.value)
  }

  @Test
  fun testEditBirthday_fromUri_failedToRead() = runTest {
    val date = LocalDate.now()
    val formattedDate = "AAA"
    val birthdayDate = "1999-10-01"

    val uri = mockk<Uri>()

    every { birthdaysDao.getById(ID) }.returns(null)
    every { dateTimeManager.parseBirthdayDate(birthdayDate) }.returns(date)
    every { dateTimeManager.formatBirthdayFullDateForUi(date) }.returns(formattedDate)
    every { dateTimeManager.getCurrentDate() }.returns(date)
    every { uriReader.readBirthdayObject(uri) }.returns(null)

    viewModel.birthday.observeForever { }
    viewModel.formattedDate.observeForever { }

    viewModel.load()
    viewModel.onContactAttached(false)
    viewModel.onFile(uri)

    verify(exactly = 1) { birthdaysDao.getById(any()) }

    assertEquals(false, viewModel.isEdited)
    assertEquals(false, viewModel.isFromFile)
    assertEquals(false, viewModel.hasSameInDb)
    assertEquals(date, viewModel.selectedDate)
    assertEquals(formattedDate, viewModel.formattedDate.value)
    assertEquals(null, viewModel.birthday.value)
  }

  @Test
  fun testEditBirthday_fromUri_sameId() = runTest {
    val objectId = "1"
    val date = LocalDate.now()
    val formattedDate = "AAA"
    val name = "Name"
    val number = ""
    val birthdayDate = "1999-10-01"

    val uri = mockk<Uri>()

    val birthdayObject = mockk<Birthday>()
    every { birthdayObject.uuId }.returns(objectId)
    every { birthdayObject.date }.returns(birthdayDate)
    every { birthdayObject.name }.returns(name)
    every { birthdayObject.number }.returns(number)
    every { birthdayObject.ignoreYear }.returns(false)

    val expectedToEdit = uiBirthdayEditAdapter.convert(birthdayObject)

    every { birthdaysDao.getById(ID) }.returns(null)
    every { birthdaysDao.getById(objectId) }.returns(birthdayObject)
    every { dateTimeManager.parseBirthdayDate(birthdayDate) }.returns(date)
    every { dateTimeManager.formatBirthdayFullDateForUi(date) }.returns(formattedDate)
    every { uriReader.readBirthdayObject(uri) }.returns(birthdayObject)

    viewModel.birthday.observeForever { }
    viewModel.formattedDate.observeForever { }

    viewModel.load()
    viewModel.onContactAttached(false)
    viewModel.onFile(uri)

    verify(exactly = 2) { birthdaysDao.getById(any()) }

    assertEquals(true, viewModel.isEdited)
    assertEquals(true, viewModel.isFromFile)
    assertEquals(true, viewModel.hasSameInDb)
    assertEquals(date, viewModel.selectedDate)
    assertEquals(formattedDate, viewModel.formattedDate.value)
    assertEquals(expectedToEdit, viewModel.birthday.value)
  }

  companion object {
    private const val ID = ""
  }
}
