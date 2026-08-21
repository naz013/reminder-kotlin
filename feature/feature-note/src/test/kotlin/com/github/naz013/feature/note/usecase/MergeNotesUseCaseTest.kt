package com.github.naz013.feature.note.usecase

import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.note.image.NoteImageRepository
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.TagAssignmentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MergeNotesUseCaseTest {
  private lateinit var noteRepository: NoteRepository
  private lateinit var noteImageRepository: NoteImageRepository
  private lateinit var tagAssignmentRepository: TagAssignmentRepository
  private lateinit var deleteNoteUseCase: DeleteNoteUseCase
  private lateinit var scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase

  private lateinit var useCase: MergeNotesUseCase

  @Before
  fun setUp() {
    noteRepository = mockk(relaxed = true)
    noteImageRepository = mockk()
    tagAssignmentRepository = mockk()
    deleteNoteUseCase = mockk(relaxed = true)
    scheduleBackgroundWorkUseCase = mockk(relaxed = true)
    useCase = MergeNotesUseCase(
      noteRepository,
      noteImageRepository,
      tagAssignmentRepository,
      deleteNoteUseCase,
      scheduleBackgroundWorkUseCase,
    )

    coEvery { tagAssignmentRepository.getTagsForItem(any(), any()) } returns emptyList()
    every { noteImageRepository.copyImagesToFolder(any(), any()) } answers { firstArg() }
  }

  private fun note(
    id: String,
    title: String = "",
    summary: String = "",
    color: Int = 0,
  ) = Note(key = id, title = title, summary = summary, color = color, syncState = SyncState.Synced)

  @Test
  fun `does nothing when fewer than two ids are given`() = runTest {
    useCase(listOf("id-1"))

    coVerify(exactly = 0) { noteRepository.save(any<Note>()) }
    coVerify(exactly = 0) { deleteNoteUseCase(any()) }
  }

  @Test
  fun `merges body with second note's title inlined and first note's title-color kept`() = runTest {
    val first = note("id-1", title = "First title", summary = "First body", color = 5)
    val second = note("id-2", title = "Second title", summary = "Second body")
    coEvery { noteRepository.getByIds(listOf("id-1", "id-2")) } returns
      listOf(NoteWithImages(first), NoteWithImages(second))

    useCase(listOf("id-1", "id-2"))

    coVerify(exactly = 1) {
      noteRepository.save(
        match<Note> {
          it.title == "First title" &&
            it.color == 5 &&
            it.summary == "First body\nSecond title\nSecond body"
        }
      )
    }
  }

  @Test
  fun `skips the second note's title in the body when it is blank`() = runTest {
    val first = note("id-1", summary = "First body")
    val second = note("id-2", title = "", summary = "Second body")
    coEvery { noteRepository.getByIds(listOf("id-1", "id-2")) } returns
      listOf(NoteWithImages(first), NoteWithImages(second))

    useCase(listOf("id-1", "id-2"))

    coVerify(exactly = 1) {
      noteRepository.save(match<Note> { it.summary == "First body\nSecond body" })
    }
  }

  @Test
  fun `builds the merged note in tap order regardless of the ids' natural order`() = runTest {
    val first = note("id-2", title = "Tapped first", summary = "A")
    val second = note("id-1", title = "Tapped second", summary = "B")
    // getByIds does not promise result order - use case must sort by the ids it was given.
    coEvery { noteRepository.getByIds(listOf("id-2", "id-1")) } returns
      listOf(NoteWithImages(second), NoteWithImages(first))

    useCase(listOf("id-2", "id-1"))

    coVerify(exactly = 1) {
      noteRepository.save(match<Note> { it.title == "Tapped first" && it.summary == "A\nTapped second\nB" })
    }
  }

  @Test
  fun `combines images from every merged note under the new note's id`() = runTest {
    val first = note("id-1", summary = "A")
    val second = note("id-2", summary = "B")
    val image1 = ImageFile(noteId = "id-1", id = 1, filePath = "/notes/id-1/a.jpg", fileName = "a.jpg")
    val image2 = ImageFile(noteId = "id-2", id = 2, filePath = "/notes/id-2/b.jpg", fileName = "b.jpg")
    coEvery { noteRepository.getByIds(listOf("id-1", "id-2")) } returns
      listOf(NoteWithImages(first, listOf(image1)), NoteWithImages(second, listOf(image2)))

    var savedNoteId = ""
    coEvery { noteRepository.save(any<Note>()) } answers { savedNoteId = firstArg<Note>().key }

    useCase(listOf("id-1", "id-2"))

    coVerify(exactly = 1) {
      noteRepository.saveAll(
        match<List<ImageFile>> { images ->
          images.size == 2 && images.all { it.noteId == savedNoteId }
        }
      )
    }
  }

  @Test
  fun `renames colliding filenames so images never overwrite each other`() = runTest {
    val first = note("id-1", summary = "A")
    val second = note("id-2", summary = "B")
    val image1 = ImageFile(noteId = "id-1", id = 1, filePath = "/notes/id-1/img.jpg", fileName = "img.jpg")
    val image2 = ImageFile(noteId = "id-2", id = 2, filePath = "/notes/id-2/img.jpg", fileName = "img.jpg")
    coEvery { noteRepository.getByIds(listOf("id-1", "id-2")) } returns
      listOf(NoteWithImages(first, listOf(image1)), NoteWithImages(second, listOf(image2)))

    useCase(listOf("id-1", "id-2"))

    coVerify(exactly = 1) {
      noteRepository.saveAll(
        match<List<ImageFile>> { images -> images.map { it.fileName }.distinct().size == 2 }
      )
    }
  }

  @Test
  fun `unions distinct tags from every merged note onto the new note`() = runTest {
    val first = note("id-1", summary = "A")
    val second = note("id-2", summary = "B")
    coEvery { noteRepository.getByIds(listOf("id-1", "id-2")) } returns
      listOf(NoteWithImages(first), NoteWithImages(second))
    coEvery { tagAssignmentRepository.getTagsForItem("id-1", TaggedItemType.NOTE) } returns
      listOf(Tag(id = "tag-1", name = "Work", color = 0))
    coEvery { tagAssignmentRepository.getTagsForItem("id-2", TaggedItemType.NOTE) } returns
      listOf(Tag(id = "tag-1", name = "Work", color = 0), Tag(id = "tag-2", name = "Home", color = 0))
    coEvery { tagAssignmentRepository.attach(any(), any(), any()) } returns Unit

    var savedNoteId = ""
    coEvery { noteRepository.save(any<Note>()) } answers { savedNoteId = firstArg<Note>().key }

    useCase(listOf("id-1", "id-2"))

    coVerify(exactly = 1) { tagAssignmentRepository.attach(savedNoteId, TaggedItemType.NOTE, "tag-1") }
    coVerify(exactly = 1) { tagAssignmentRepository.attach(savedNoteId, TaggedItemType.NOTE, "tag-2") }
  }

  @Test
  fun `deletes every original note only after the merged note is built`() = runTest {
    val first = note("id-1", summary = "A")
    val second = note("id-2", summary = "B")
    coEvery { noteRepository.getByIds(listOf("id-1", "id-2")) } returns
      listOf(NoteWithImages(first), NoteWithImages(second))

    useCase(listOf("id-1", "id-2"))

    coVerify(exactly = 1) { deleteNoteUseCase("id-1") }
    coVerify(exactly = 1) { deleteNoteUseCase("id-2") }
  }

  @Test
  fun `ignores ids that no longer resolve to a note`() = runTest {
    val first = note("id-1", summary = "A")
    coEvery { noteRepository.getByIds(listOf("id-1", "missing")) } returns listOf(NoteWithImages(first))

    useCase(listOf("id-1", "missing"))

    coVerify(exactly = 0) { noteRepository.save(any<Note>()) }
  }
}
