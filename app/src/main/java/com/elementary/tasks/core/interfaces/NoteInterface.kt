package com.elementary.tasks.core.interfaces

interface NoteInterface {
    fun getSummary(): String
    fun getKey(): String
    fun getGmtTime(): String
    fun getColor(): Int
    fun getPalette(): Int
    fun getStyle(): Int
    fun getOpacity(): Int
}