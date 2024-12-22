package com.elementary.tasks.core.cloud.converters

import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.note.OldNote
import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.github.naz013.cloudapi.legacy.Convertible
import com.github.naz013.cloudapi.legacy.Metadata
import com.github.naz013.logging.Logger
import java.io.InputStream
import java.lang.ref.WeakReference

class NoteConverter(
  private val noteToOldNoteConverter: NoteToOldNoteConverter,
  private val memoryUtil: MemoryUtil
) : Convertible<NoteWithImages> {

  override fun metadata(t: NoteWithImages): Metadata {
    return Metadata(
      t.getKey(),
      t.getKey() + FileConfig.FILE_NAME_NOTE,
      FileConfig.FILE_NAME_NOTE,
      t.getGmtTime(),
      "Place Backup"
    )
  }

  override fun toOutputStream(t: NoteWithImages): CopyByteArrayStream? {
    val oldNote = noteToOldNoteConverter.toOldNote(t) ?: return null
    val stream = CopyByteArrayStream()
    memoryUtil.toStream(oldNote, stream)
    return stream
  }

  override fun convert(stream: InputStream): NoteWithImages? {
    return try {
      val weakNote = WeakReference(MemoryUtil.fromStream(stream, OldNote::class.java))
      stream.close()
      val oldNote = weakNote.get() ?: return null
      return noteToOldNoteConverter.toNote(oldNote)
    } catch (e: Exception) {
      Logger.e("NoteConverter: convert error: $e")
      null
    }
  }
}
