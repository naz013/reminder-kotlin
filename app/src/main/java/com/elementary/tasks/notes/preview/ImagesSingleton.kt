package com.elementary.tasks.notes.preview

import com.elementary.tasks.core.data.models.ImageFile

class ImagesSingleton {

    private var images: MutableList<ImageFile> = mutableListOf()
    private var imageFile: ImageFile? = null
    private var wasEdited: Boolean = false

    fun setEditable(imageFile: ImageFile) {
        this.imageFile = imageFile
        wasEdited = true
    }

    fun getEditable(): ImageFile? {
        return if (wasEdited) {
            wasEdited = false
            imageFile
        } else null
    }

    fun getCurrent(): List<ImageFile> = images

    fun clear() {
        images.clear()
    }

    fun setCurrent(images: List<ImageFile>) {
        clear()
        this.images.addAll(images)
    }
}