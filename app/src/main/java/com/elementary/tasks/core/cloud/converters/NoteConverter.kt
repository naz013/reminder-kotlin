package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.OldNote
import com.elementary.tasks.core.utils.io.CopyByteArrayStream
import com.elementary.tasks.core.utils.io.MemoryUtil
import timber.log.Timber
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

  override fun convert(t: NoteWithImages): FileIndex? {
    return try {
      val stream = CopyByteArrayStream()
      memoryUtil.toStream(t, stream)
      FileIndex().apply {
        this.stream = stream
        this.ext = FileConfig.FILE_NAME_NOTE
        this.id = t.getKey()
        this.updatedAt = t.getGmtTime()
        this.type = IndexTypes.TYPE_NOTE
        this.readyToBackup = true
      }
    } catch (e: Exception) {
      Timber.e(e)
      null
    }
  }

  override fun convert(stream: InputStream): NoteWithImages? {
    return try {
      val weakNote = WeakReference(MemoryUtil.fromStream(stream, OldNote::class.java))
      stream.close()
      val oldNote = weakNote.get() ?: return null
      return noteToOldNoteConverter.toNote(oldNote)
    } catch (e: Exception) {
      Timber.e(e)
      null
    }
  }
}
