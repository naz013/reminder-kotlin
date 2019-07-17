package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.models.NoteWithImages

class NoteRepository : DatabaseRepository<NoteWithImages>() {
    override suspend fun get(id: String): NoteWithImages? {
        return appDb.notesDao().getById(id)
    }

    override suspend fun insert(t: NoteWithImages) {
        val note = t.note
        if (note != null) {
            appDb.notesDao().insert(note)
            appDb.notesDao().insertAll(t.images)
        }
    }

    override suspend fun all(): List<NoteWithImages> {
        return appDb.notesDao().all()
    }

    override suspend fun delete(t: NoteWithImages) {
        val note = t.note ?: return
        appDb.notesDao().delete(note)
        for (image in t.images) {
            appDb.notesDao().delete(image)
        }
    }
}