package com.elementary.tasks.core.arch

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ImportIntentResolverTest {

  private lateinit var resolver: ImportIntentResolver

  @Before
  fun setUp() {
    resolver = ImportIntentResolver()
  }

  @Test
  fun `isSupportedScheme returns true for content scheme`() {
    assertTrue(resolver.isSupportedScheme("content"))
  }

  @Test
  fun `isSupportedScheme returns true for file scheme`() {
    assertTrue(resolver.isSupportedScheme("file"))
  }

  @Test
  fun `isSupportedScheme returns false for null scheme`() {
    assertEquals(false, resolver.isSupportedScheme(null))
  }

  @Test
  fun `isSupportedScheme returns false for unrelated scheme`() {
    assertEquals(false, resolver.isSupportedScheme("http"))
  }

  @Test
  fun `resolve returns Unsupported when data is null`() {
    assertEquals(ImportResult.Unsupported, resolver.resolve(null))
  }

  @Test
  fun `resolve returns Unsupported for an unrecognized type`() {
    assertEquals(ImportResult.Unsupported, resolver.resolve("not a supported object"))
  }

  @Test
  fun `resolve returns Valid for a valid Place`() {
    val place = place()

    val result = resolver.resolve(place)

    assertEquals(ImportResult.Valid(place), result)
  }

  @Test
  fun `resolve returns Invalid with latitude reason when Place latitude is zero`() {
    val place = place(latitude = 0.0)

    val result = resolver.resolve(place)

    assertEquals(ImportResult.Invalid("Latitude is 0"), result)
  }

  @Test
  fun `resolve returns Invalid with longitude reason when Place longitude is zero`() {
    val place = place(longitude = 0.0)

    val result = resolver.resolve(place)

    assertEquals(ImportResult.Invalid("Longitude is 0"), result)
  }

  @Test
  fun `resolve returns Invalid with name reason when Place name is blank`() {
    val place = place(name = " ")

    val result = resolver.resolve(place)

    assertEquals(ImportResult.Invalid("Name is blank"), result)
  }

  @Test
  fun `resolve returns Valid for a valid Birthday`() {
    val birthday = birthday()

    val result = resolver.resolve(birthday)

    assertEquals(ImportResult.Valid(birthday), result)
  }

  @Test
  fun `resolve returns Invalid with name reason when Birthday name is blank`() {
    val birthday = birthday(name = "")

    val result = resolver.resolve(birthday)

    assertEquals(ImportResult.Invalid("Name is blank"), result)
  }

  @Test
  fun `resolve returns Invalid with date reason when Birthday date is blank`() {
    val birthday = birthday(date = "")

    val result = resolver.resolve(birthday)

    assertEquals(ImportResult.Invalid("Date is blank"), result)
  }

  @Test
  fun `resolve returns Invalid with key reason when Birthday uuId is blank`() {
    val birthday = birthday(uuId = "")

    val result = resolver.resolve(birthday)

    assertEquals(ImportResult.Invalid("Key is blank"), result)
  }

  @Test
  fun `resolve returns Invalid with day reason when Birthday day is zero`() {
    val birthday = birthday(day = 0)

    val result = resolver.resolve(birthday)

    assertEquals(ImportResult.Invalid("Day is 0"), result)
  }

  @Test
  fun `resolve returns Valid for a NoteWithImages with a non-empty key`() {
    val note = noteWithImages()

    val result = resolver.resolve(note)

    assertEquals(ImportResult.Valid(note), result)
  }

  @Test
  fun `resolve returns Invalid when NoteWithImages has a null note`() {
    val note = NoteWithImages(note = null)

    val result = resolver.resolve(note)

    assertEquals(ImportResult.Invalid("Note is not valid"), result)
  }

  @Test
  fun `resolve returns Invalid when NoteWithImages note key is empty`() {
    val note = noteWithImages(key = "")

    val result = resolver.resolve(note)

    assertEquals(ImportResult.Invalid("Note is not valid"), result)
  }

  @Test
  fun `resolve always returns Valid for ReminderV2`() {
    val reminder = ReminderV2(schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

    val result = resolver.resolve(reminder)

    assertEquals(ImportResult.Valid(reminder), result)
  }

  @Test
  fun `resolve always returns Valid for GroupV2`() {
    val group = GroupV2()

    val result = resolver.resolve(group)

    assertEquals(ImportResult.Valid(group), result)
  }

  private fun place(
    latitude: Double = 10.0,
    longitude: Double = 20.0,
    name: String = "Home",
  ) = Place(
    latitude = latitude,
    longitude = longitude,
    name = name,
    syncState = SyncState.Synced,
  )

  private fun birthday(
    name: String = "John",
    date: String = "01.01.2000",
    uuId: String = "uuid-1",
    day: Int = 1,
  ) = Birthday(
    name = name,
    date = date,
    uuId = uuId,
    day = day,
    syncState = SyncState.Synced,
  )

  private fun noteWithImages(key: String = "note-key") = NoteWithImages(
    note = Note(key = key, syncState = SyncState.Synced),
  )
}
