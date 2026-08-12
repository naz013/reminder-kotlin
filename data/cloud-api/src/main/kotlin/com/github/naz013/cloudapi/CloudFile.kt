package com.github.naz013.cloudapi

data class CloudFile(
  val id: String = "",
  val name: String,
  val fileDescription: String? = null,
  val lastModified: Long = 0L,
  val size: Int = 0,
  val fileExtension: String,
  val version: Long = 0L,
  val rev: String = "",
)
