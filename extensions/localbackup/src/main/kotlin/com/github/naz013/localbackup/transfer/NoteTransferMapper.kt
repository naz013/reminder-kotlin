package com.github.naz013.localbackup.transfer

import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.combineLegacyNoteColor
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.model.NoteV3Image
import com.github.naz013.files.model.NoteV4Json
import com.github.naz013.sync.images.toNoteTextSpans
import com.github.naz013.sync.images.toV4Spans
import java.io.File

/**
 * Note<->[NoteV4Json] mapping for the cross-app transfer feature, mirroring the field list
 * `data:sync`'s `PreProcessUploadingFileUseCase`/`PostProcessNoteV4UseCase` use for cloud sync
 * (span conversion itself is reused directly from there via [toV4Spans]/[toNoteTextSpans]). `images`
 * carries only file names here - the transfer package writer/reader place the actual bytes as
 * separate zip entries rather than routing them through a cloud-file `id`, since there's no cloud
 * upload involved.
 */
internal fun Note.toTransferJson(images: List<ImageFile>): NoteV4Json = NoteV4Json(
  key = key,
  text = content.text,
  spans = content.toV4Spans(),
  color = color,
  archived = archived,
  isPinned = isPinned,
  date = date,
  fontSize = fontSize,
  style = style,
  uniqueId = uniqueId,
  updatedAt = updatedAt,
  version = version,
  images = images.map { image ->
    NoteV3Image(
      fileName = image.fileName,
      size = File(image.filePath).let { if (it.exists()) it.length().toInt() else 0 }
    )
  }
)

internal fun NoteV4Json.toTransferNote(): Note = Note(
  color = combineLegacyNoteColor(color, palette),
  key = key,
  date = date,
  style = style,
  uniqueId = uniqueId,
  content = NoteDocument(text = text, spans = spans.toNoteTextSpans()),
  updatedAt = updatedAt,
  fontSize = fontSize,
  archived = archived,
  isPinned = isPinned,
  version = version,
  syncState = SyncState.WaitingForUpload
)
