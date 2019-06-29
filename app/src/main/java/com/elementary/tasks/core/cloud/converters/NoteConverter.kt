package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.OldNote
import com.elementary.tasks.core.utils.MemoryUtil
import com.google.gson.Gson
import java.lang.ref.WeakReference

class NoteConverter : Convertible<NoteWithImages> {

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
            val json = Gson().toJson(OldNote(t))
            val encrypted = MemoryUtil.encryptJson(json)
            FileIndex().apply {
                this.json = encrypted
                this.ext = FileConfig.FILE_NAME_NOTE
                this.id = t.getKey()
                this.updatedAt = t.getGmtTime()
                this.type = IndexTypes.TYPE_NOTE
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun convert(encrypted: String): NoteWithImages? {
        if (encrypted.isEmpty()) return null
        return try {
            val json = MemoryUtil.decryptToJson(encrypted) ?: return null
            val weakNote = WeakReference(Gson().fromJson(json, OldNote::class.java))
            val oldNote = weakNote.get() ?: return null
            val noteWithImages = NoteWithImages()
            oldNote.images.forEach {
                it.noteId = oldNote.key
            }
            noteWithImages.note = Note(oldNote)
            noteWithImages.images = oldNote.images
            return noteWithImages
        } catch (e: Exception) {
            null
        }
    }
}