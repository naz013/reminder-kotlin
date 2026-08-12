package com.github.naz013.platform

interface StringApi {
  fun getString(id: Int): String
  fun getString(id: Int, vararg args: Any): String
  fun getStringArray(id: Int): Array<String>
}
