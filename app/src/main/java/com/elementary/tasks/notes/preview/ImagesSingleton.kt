package com.elementary.tasks.notes.preview

import com.elementary.tasks.notes.create.NoteImage

class ImagesSingleton {

    private var images: MutableList<NoteImage> = mutableListOf()

    fun getCurrent(): List<NoteImage> = images

    fun clear() {
        images.clear()
    }

    fun setCurrent(images: List<NoteImage>) {
        clear()
        this.images.addAll(images)
    }
}