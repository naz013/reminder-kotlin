package com.github.naz013.logic.note

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.demophoto.DemoPhotoDownloader
import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState

/**
 * Seeds a few showcase notes on first install: one demonstrating rich-text formatting, one a
 * short practical checklist, and one with an attached photo. All three go through
 * [SaveNoteUseCase] - the same path a real user's note takes - so they get sync state and
 * background upload scheduling like any other note. The photo is fetched via
 * [DemoPhotoDownloader], which never throws - a failed/offline fetch just means the photo note
 * is saved without an image rather than blocking the rest of the demo content.
 */
class InsertDemoNotesUseCase(
  private val saveNoteUseCase: SaveNoteUseCase,
  private val noteImageRepository: NoteImageRepository,
  private val demoPhotoDownloader: DemoPhotoDownloader,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke() {
    saveNoteUseCase(NoteWithImages(note = buildWelcomeNote()))
    saveNoteUseCase(NoteWithImages(note = buildTripIdeasNote()))
    insertPhotoNote()
  }

  private suspend fun insertPhotoNote() {
    val photo = demoPhotoDownloader.downloadRandomWallpaper()
    val note = buildPhotoNote(photo?.photographerName)
    if (photo == null) {
      saveNoteUseCase(NoteWithImages(note = note))
      return
    }
    val tmpPath = noteImageRepository.saveTemporaryImage(PHOTO_FILE_NAME, photo.bytes.inputStream())
    saveNoteUseCase(
      NoteWithImages(
        note = note,
        images = listOf(ImageFile(fileName = PHOTO_FILE_NAME, filePath = tmpPath)),
      ),
    )
  }

  private fun buildWelcomeNote(): Note {
    val title = "Welcome to Reminder"
    val intro = "This app helps you stay on top of tasks, birthdays, and ideas — all in one place."
    val subheading = "Try these features"
    val bullet1 = "Create reminders with calls, texts, links, or shopping lists"
    val bullet2 = "Get notified for upcoming birthdays automatically"
    val bullet3 = "Write richly formatted notes with bold, colors, and more"
    val bullet4 = "Attach photos to your notes for quick visual reminders"
    val outro = "Enjoy exploring the app!"

    val text = listOf(title, intro, subheading, bullet1, bullet2, bullet3, bullet4, outro).joinToString("\n")

    val spans = mutableListOf<NoteTextSpan>()
    spans += spanOf(text, title, NoteSpanAttribute.Heading1)
    spans += spanOf(text, subheading, NoteSpanAttribute.Heading2)
    spans += spanOf(text, bullet1, NoteSpanAttribute.BulletItem)
    spans += spanOf(text, bullet2, NoteSpanAttribute.BulletItem)
    spans += spanOf(text, bullet3, NoteSpanAttribute.BulletItem)
    spans += spanOf(text, bullet4, NoteSpanAttribute.BulletItem)
    spans += spanOf(text, "calls, texts, links", NoteSpanAttribute.Bold)
    spans += spanOf(text, "automatically", NoteSpanAttribute.Italic)
    spans += spanOf(text, "richly formatted", NoteSpanAttribute.Underline)
    spans += spanOf(text, "photos", NoteSpanAttribute.SolidColor(WELCOME_ACCENT_COLOR))
    spans += spanOf(
      text,
      outro,
      NoteSpanAttribute.GradientColor(colors = WELCOME_GRADIENT_COLORS, angleDegrees = 45f),
    )

    return Note(
      content = NoteDocument(text = text, spans = spans),
      color = LIGHT_BLUE_COLOR_INDEX,
      date = dateTimeManager.getNowGmtDateTime(),
      syncState = SyncState.Synced,
    )
  }

  private fun buildTripIdeasNote(): Note {
    val title = "Weekend Trip Ideas"
    val bullet1 = "Hike the coastal trail"
    val bullet2 = "Visit the farmers market"
    val bullet3 = "Try that new ramen place downtown"
    val bullet4 = "Sunset photos at the pier"

    val text = listOf(title, bullet1, bullet2, bullet3, bullet4).joinToString("\n")
    val spans = mutableListOf<NoteTextSpan>()
    spans += spanOf(text, title, NoteSpanAttribute.Heading1)
    spans += spanOf(text, bullet1, NoteSpanAttribute.BulletItem)
    spans += spanOf(text, bullet2, NoteSpanAttribute.BulletItem)
    spans += spanOf(text, bullet3, NoteSpanAttribute.BulletItem)
    spans += spanOf(text, bullet4, NoteSpanAttribute.BulletItem)

    return Note(
      content = NoteDocument(text = text, spans = spans),
      color = GREEN_COLOR_INDEX,
      date = dateTimeManager.getNowGmtDateTime(),
      syncState = SyncState.Synced,
    )
  }

  private fun buildPhotoNote(photographerName: String?): Note {
    val title = "A Little Inspiration"
    val caption = "A little inspiration for your next note — attach photos just like this one."
    val attribution = photographerName?.let { "Photo by $it on Unsplash" }
    val text = listOfNotNull(title, caption, attribution).joinToString("\n")

    val spans = mutableListOf(spanOf(text, title, NoteSpanAttribute.Heading1))
    attribution?.let { spans += spanOf(text, it, NoteSpanAttribute.Italic) }

    return Note(
      content = NoteDocument(text = text, spans = spans),
      color = PINK_COLOR_INDEX,
      date = dateTimeManager.getNowGmtDateTime(),
      syncState = SyncState.Synced,
    )
  }

  private fun spanOf(text: String, value: String, attribute: NoteSpanAttribute): NoteTextSpan {
    val start = text.indexOf(value)
    return NoteTextSpan(start, start + value.length, attribute)
  }

  companion object {
    private const val PHOTO_FILE_NAME = "demo_note_photo.jpg"

    // Matches ui-common's ThemeProvider.AppColorIndex palette indices (LIGHT_BLUE/GREEN/PINK) -
    // not imported directly here to avoid a logic -> ui dependency.
    private const val LIGHT_BLUE_COLOR_INDEX = 6
    private const val GREEN_COLOR_INDEX = 9
    private const val PINK_COLOR_INDEX = 1

    private val WELCOME_ACCENT_COLOR = 0xFF1E88E5.toInt()
    private val WELCOME_GRADIENT_COLORS = listOf(0xFF8E24AA.toInt(), 0xFFFB8C00.toInt())
  }
}
