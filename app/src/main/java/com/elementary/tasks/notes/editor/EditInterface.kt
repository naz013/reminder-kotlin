package com.elementary.tasks.notes.editor

interface EditInterface {
    fun getOriginal(): ByteArray?

    fun saveCurrent(byteArray: ByteArray?)

    fun getCurrent(): ByteArray?
}