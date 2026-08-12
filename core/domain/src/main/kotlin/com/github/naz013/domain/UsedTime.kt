package com.github.naz013.domain

data class UsedTime(
  val id: Long = 0,
  val timeString: String = "",
  val timeMills: Long = 0,
  val useCount: Int = 0
)
