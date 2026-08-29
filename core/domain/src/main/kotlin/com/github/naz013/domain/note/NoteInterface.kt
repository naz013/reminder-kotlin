package com.github.naz013.domain.note

interface NoteInterface {
  fun getSummary(): String
  fun getKey(): String
  fun getGmtTime(): String
  fun getColor(): Int
  fun getStyle(): Int
  fun getOpacity(): Int
}
